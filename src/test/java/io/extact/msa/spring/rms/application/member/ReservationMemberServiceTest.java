package io.extact.msa.spring.rms.application.member;

import static io.extact.msa.spring.rms.application.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import io.extact.msa.spring.platform.test.stub.auth.TestAuthUtils;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.domain.reservation.ReservationCreator;
import io.extact.msa.spring.rms.domain.reservation.ReservationDuplicateChecker;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;
import io.extact.msa.spring.test.assertj.ToStringAssert;

@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class ReservationMemberServiceTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {};
    private static final int WITH_SIDE_EFFECT_CASE = 99;

    @Autowired
    private ReservationMemberService service;

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
        ReservationMemberService reservationMemberService(
                ReservationCreator modelCreator,
                ReservationModelComposer modelComposer,
                ReservationDuplicateChecker duplicateChecker,
                ReservationRepository reservationRepository,
                ItemRepository itemRepository,
                UserRepository userRepository) {

            return new ReservationMemberService(
                    modelCreator,
                    modelComposer,
                    duplicateChecker,
                    reservationRepository,
                    itemRepository,
                    userRepository);
        }
    }

    @BeforeEach
    void beforeEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    void testGetItemAll() {
        // when
        List<ItemReference> items = service.getItemAll();
        // then
        ToStringAssert.assertThatToString(items).containsExactly(item1, item2, item3, item4);
    }

    @Test
    void testFindCanRentedItemAtPeriod() { // 絞り込まれた結果
        // given
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 2, 23, 59, 0);

        // when
        List<ItemReference> items = service.findCanRentedItemAtPeriod(from, to);
        // then
        ToStringAssert.assertThatToString(items).containsExactly(item1, item2, item4);
    }
    @Test
    void testFindCanRentedItemAtPeriodOnResultAll() { // 全件該当
        // given
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusHours(1);

        // when
        List<ItemReference> items = service.findCanRentedItemAtPeriod(from, to);
        // then
        ToStringAssert.assertThatToString(items).containsExactly(item1, item2, item3, item4);
    }

    @Test
    void testCanRentedItemAtPeriodOK() {
        // given
        ItemId itemId = new ItemId(3);
        LocalDateTime from = LocalDateTime.of(2020, 4, 2, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 2, 12, 0, 0);

        // when
        boolean result = service.canRentedItemAtPeriod(itemId, from, to);
        // then
        assertThat(result).isTrue();
    }

    @Test
    void testCanRentedItemAtPeriodNG() {
        // given
        ItemId itemId = new ItemId(3);
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 12, 0, 0);

        // when
        boolean result = service.canRentedItemAtPeriod(itemId, from, to);
        // then
        assertThat(result).isFalse();
    }

    @Test
    void testFindReservationByItemId() {
        // given
        ItemId itemId = new ItemId(3);

        // when
        List<ReservationComposeModel> reservations = service.findReservationByItemId(itemId);
        // then
        ToStringAssert.assertThatToString(reservations).containsExactly(model1, model2, model3);
    }

    @Test
    void testFindReservationByItemIdOnNotFound() {
        // given
        ItemId itemId = new ItemId(1);

        // when
        List<ReservationComposeModel> reservations = service.findReservationByItemId(itemId);
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    void testFindReservationByItemIdAndFromDate() {
        // given
        ItemId itemId = new ItemId(3);
        LocalDate fromDate = LocalDate.of(2020, 4, 1);

        // when
        List<ReservationComposeModel> reservations = service.findReservationByItemIdAndFromDate(itemId, fromDate);
        // then
        ToStringAssert.assertThatToString(reservations).containsExactly(model1, model2);
    }

    @Test
    void testFindReservationByItemIdAndFromDateOnNotFound() {
        // given
        ItemId itemId = new ItemId(3);
        LocalDate fromDate = LocalDate.of(2019, 4, 1);

        // when
        List<ReservationComposeModel> reservations = service.findReservationByItemIdAndFromDate(itemId, fromDate);
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    void testFindReservationByReserverId() {
        // given
        UserId reserverId = new UserId(1);

        // when
        List<ReservationComposeModel> reservations = service.findReservationByReserverId(reserverId);
        // then
        ToStringAssert.assertThatToString(reservations).containsExactly(model1, model3);
    }

    @Test
    void testFindReservationByReserverIdOnNotFound() {
        // given
        UserId reserverId = new UserId(9);

        // when
        List<ReservationComposeModel> reservations = service.findReservationByReserverId(reserverId);
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    void testGetOwnReservations() {
        // given
        int reserverId = 1;
        TestAuthUtils.signin(reserverId, "MEMBER");

        // when
        List<ReservationComposeModel> reservations = service.getOwnReservations();
        // then
        ToStringAssert.assertThatToString(reservations).containsExactly(model1, model3);
    }

    @Test
    void testGetOwnReservationsOnNotFound() {
        // given
        int reserverId = 3;
        TestAuthUtils.signin(reserverId, "MEMBER");

        // when
        List<ReservationComposeModel> reservations = service.getOwnReservations();
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testReserve(@Autowired ReservationRepository forResultAssert) {
        // given
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)))
                .note("note")
                .itemId(new ItemId(1))
                .reserverId(new UserId(1))
                .build();

        // when
        ReservationComposeModel actual = service.reserve(command);

        // then
        Reservation added = testCreator.newInstance(
                new ReservationId(1000),
                command.period(),
                command.note(),
                command.itemId(),
                command.reserverId());
        ReservationComposeModel expected = new ReservationComposeModel(added, item1, user1);
        ToStringAssert.assertThatToString(actual).isEqualTo(expected);
        // commitされているか確認するため読み返し
        Optional<Reservation> persisted = forResultAssert.find(added.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(added);
    }

    @Test
    void testReserveOnDuplicate(@Autowired ReservationRepository forResultAssert) {

        // -- 事前条件
        LocalDateTime from = LocalDateTime.now().plusHours(1);
        LocalDateTime to = from.plusHours(2);
        ReserveItemCommand preCommand = ReserveItemCommand.builder()
                .period(new ReservationPeriod(from, to))
                .note("note")
                .itemId(new ItemId(3))
                .reserverId(new UserId(3))
                .build();
        ReservationComposeModel preCondition = service.reserve(preCommand);

        // given
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        preCondition.reservation().getPeriod().getFrom().plusHours(1),
                        preCondition.reservation().getPeriod().getTo().plusHours(1)))
                .note("note")
                .itemId(new ItemId(3))
                .reserverId(new UserId(3))
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.reserve(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.DUPLICATE);
        // 永続化確認（エラー時は重複データはそのまま）
        Reservation persisted = forResultAssert
                .findOverlappingReservations(
                        command.itemId(),
                        command.period())
                .getFirst();
        assertThat(persisted).isEqualTo(persisted);
    }

    @Test
    void testReserveOnValidationErrorOfProperty(@Autowired ReservationRepository forResultAssert) {
        // given
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().minusDays(1), // 過去日付エラー
                        LocalDateTime.now()))
                .note("note")
                .itemId(new ItemId(1))
                .reserverId(new UserId(1))
                .build();

        // when
        RmsValidationException exception = assertThrows(RmsValidationException.class, () -> {
            service.reserve(command);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(exception)
                .verifyErrorItemFieldOf("Reservation.period.from");
        // 永続化確認（エラー時は増加なし）
        List<Reservation> reservations = forResultAssert.findAll();
        assertThat(reservations).hasSize(3);
    }

    @Test
    void testReserveOnItemNotExist(@Autowired ReservationRepository forResultAssert) {
        // given
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)))
                .note("note")
                .itemId(new ItemId(999)) // unknown item
                .reserverId(new UserId(1))
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.reserve(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
        assertThat(exception).hasMessageContaining("itemId");
    }

    @Test
    void testReserveOnUserNotExist(@Autowired ReservationRepository forResultAssert) {
        // given
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)))
                .note("note")
                .itemId(new ItemId(1))
                .reserverId(new UserId(999)) // unknown user
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.reserve(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
        assertThat(exception).hasMessageContaining("reserverId");
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testCancel(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId cancelId = reservation3.getId();
        int reserverId = 1;
        TestAuthUtils.signin(reserverId, "MEMBER");

        // when
        service.cancel(cancelId);

        // then
        Optional<Reservation> persisted = forResultAssert.find(cancelId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    void testCancelOnOthreUserReservationCancel(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId cancelId = reservation1.getId();
        int reserverId = 3;
        TestAuthUtils.signin(reserverId, "MEMBER");

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.cancel(cancelId);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.FORBIDDEN);
        // 削除されていないことの確認
        Optional<Reservation> persisted = forResultAssert.find(cancelId);
        assertThat(persisted).isPresent();
    }

    @Test
    void testDeleteOnNotFound(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId cancelId = new ReservationId(99);
        int reserverId = 1;
        TestAuthUtils.signin(reserverId, "MEMBER");

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.cancel(cancelId);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }
}
