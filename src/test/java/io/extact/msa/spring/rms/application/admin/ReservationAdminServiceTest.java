package io.extact.msa.spring.rms.application.admin;

import static io.extact.msa.spring.rms.application.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.async.AsyncConfig;
import io.extact.msa.spring.platform.core.async.AsyncInvoker;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.reservation.ReservationDuplicateChecker;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;
import io.extact.msa.spring.test.assertj.ToStringAssert;

@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class ReservationAdminServiceTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {};
    private static final int WITH_SIDE_EFFECT_CASE = 99;

    @Autowired
    private ReservationAdminService service;

    @Configuration(proxyBeanMethods = false)
    @Import({
        PersistenceConfig.class,
        DomainConfig.class,
        AsyncConfig.class})
    static class TestConfig {

        @Bean
        ReservationModelComposer modelComposer(
                ItemRepository itemRepository,
                UserRepository userRepository,
                AsyncInvoker asyncInvoker) {
            return new ReservationModelComposer(itemRepository, userRepository, asyncInvoker);
        }

        @Bean
        ReservationAdminService reservationAdminService(
                ReservationDuplicateChecker duplicateChecker,
                ReservationModelComposer modelComposer,
                ReservationRepository repository) {
            return new ReservationAdminService(duplicateChecker, modelComposer, repository);
        }
    }

    @Test
    void testGetAll() {
        // when
        List<ReservationComposeModel> reservations = service.getAll();
        // then
        ToStringAssert.assertThatToString(reservations).containsExactly(model1, model2, model3);
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testUpdate(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationUpdateCommand command = ReservationUpdateCommand.builder()
                .id(reservation1.getId())
                .period(new ReservationPeriod(LocalDateTime.of(2025, 1, 1, 10, 0), LocalDateTime.of(2025, 1, 1, 13, 0)))
                .note("Updated Team meeting")
                .build();

        // when
        ReservationComposeModel actual = service.update(command);

        // then
        Reservation updated = testCreator.newInstance(
                command.id(),
                command.period(),
                command.note(),
                reservation3.getItemId(),
                reservation1.getReserverId());
        ReservationComposeModel expected = new ReservationComposeModel(updated, item3, user1);
        ToStringAssert.assertThatToString(actual).isEqualTo(expected);
        // commitされているかの確認
        Optional<Reservation> persisted = forResultAssert.find(reservation1.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(updated);
    }

    @Test
    void testUpdateOnNotFound(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId notFoundId = new ReservationId(99);
        ReservationUpdateCommand command = ReservationUpdateCommand.builder()
                .id(notFoundId)
                .period(new ReservationPeriod(LocalDateTime.of(2025, 1, 4, 10, 0), LocalDateTime.of(2025, 1, 4, 12, 0)))
                .note("Non-existent Reservation")
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.update(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
        // 追加されていないことの確認
        Optional<Reservation> persisted = forResultAssert.find(notFoundId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    void testUpdateOnDuplicate(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationUpdateCommand command = ReservationUpdateCommand.builder()
                .id(reservation1.getId())
                .period(reservation2.getPeriod()) // reservation2 の期間と重複
                .note("Duplicate Reservation")
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.update(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.DUPLICATE);
        // commitされていないことの確認(reservation1がそのまま)
        Optional<Reservation> persisted = forResultAssert.find(reservation1.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(reservation1);
    }

    @Test
    void testUpdateOnValidationErrorOfProperty(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationUpdateCommand command = ReservationUpdateCommand.builder()
                .id(reservation1.getId())
                .period(new ReservationPeriod(
                        LocalDateTime.of(2025, 1, 1, 12, 0),
                        LocalDateTime.of(2025, 1, 1, 11, 0))) // 終了時間が開始時間より前
                .note("Invalid Reservation")
                .build();

        // when
        RmsValidationException exception = assertThrows(RmsValidationException.class, () -> {
            service.update(command);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(exception)
                .verifyErrorItemFieldOf("Reservation.period");
        // commitされていないことの確認(reservation1がそのまま)
        Optional<Reservation> persisted = forResultAssert.find(reservation1.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(reservation1);
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testDelete(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId deleteId = reservation3.getId();

        // when
        service.delete(deleteId);

        // then
        Optional<Reservation> persisted = forResultAssert.find(deleteId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    void testDeleteOnNotFound(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId notFoundId = new ReservationId(99); // 存在しないID

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.delete(notFoundId);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }
}
