package io.extact.msa.spring.rms.application.member;

import static io.extact.msa.spring.platform.fw.test.utils.IsEqualableAssert.*;
import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.core.async.AsyncConfig;
import io.extact.msa.spring.platform.core.async.AsyncInvoker;
import io.extact.msa.spring.platform.core.auth.context.DefaultLoginContext;
import io.extact.msa.spring.platform.core.auth.context.LoginContext;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.feature.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.test.utils.RmsValidationExceptionAsserter;
import io.extact.msa.spring.platform.fw.test.utils.TestAuthUtils;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;
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

@DataJpaTest // default rollback
@ActiveProfiles({ "test", "jpa-all" })
class ReserveItemServiceTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {};

    @Autowired
    private ReserveItemService service;

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(TaskExecutionAutoConfiguration.class)
    @Import({
            PersistenceConfig.class,
            DomainConfig.class,
            AsyncConfig.class })
    static class TestConfig {

        @Bean
        LoginContext loginContext() {
            return new DefaultLoginContext();
        }

        @Bean
        ReservationModelComposer modelComposer(
                ItemRepository itemRepository,
                UserRepository userRepository,
                AsyncInvoker asyncInvoker) {
            return new ReservationModelComposer(itemRepository, userRepository, asyncInvoker);
        }

        @Bean
        ReserveItemService reservationMemberService(
                LoginContext loginContext,
                ReservationCreator modelCreator,
                ReservationModelComposer modelComposer,
                ReservationDuplicateChecker duplicateChecker,
                ReserveItemQueryService queryService,
                ReservationRepository reservationRepository,
                ItemRepository itemRepository,
                UserRepository userRepository) {

            return new ReserveItemService(
                    loginContext,
                    modelCreator,
                    modelComposer,
                    duplicateChecker,
                    queryService,
                    reservationRepository,
                    itemRepository,
                    userRepository);
        }
    }

    @BeforeEach
    public void beforeEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    public void testGetItemAll() {
        // when
        List<ItemModelView> items = service.getItemAll();
        // then
        ToStringAssert.assertThatToString(items).containsExactly(item1, item2, item3, item4);
    }

    @Test
    public void testFindCanRentedItemAtPeriod() { // 絞り込まれた結果
        // given
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 2, 23, 59, 0);

        // when
        List<ItemModelView> items = service.findRentableItemAtPeriod(from, to);
        // then
        ToStringAssert.assertThatToString(items).containsExactly(item1, item2, item4);
    }

    @Test
    public void testFindCanRentedItemAtPeriodOnResultAll() { // 全件該当
        // given
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusHours(1);

        // when
        List<ItemModelView> items = service.findRentableItemAtPeriod(from, to);
        // then
        ToStringAssert.assertThatToString(items).containsExactly(item1, item2, item3, item4);
    }

    @Test
    public void testCanRentedItemAtPeriodOK() {
        // given
        ItemId itemId = new ItemId(3);
        LocalDateTime from = LocalDateTime.of(2020, 4, 2, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 2, 12, 0, 0);

        // when
        boolean result = service.isRentableItemAtPeriod(itemId, from, to);
        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testCanRentedItemAtPeriodNG() {
        // given
        ItemId itemId = new ItemId(3);
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 12, 0, 0);

        // when
        boolean result = service.isRentableItemAtPeriod(itemId, from, to);
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testFindReservationByItemId() {
        // given
        Integer itemId = 3;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .build();
        // when
        List<ReservationComposeModel> reservations = service.findReservationByCondition(cond);
        // then
        assertThatByEqualable(reservations).containsExactly(model1, model2, model3);
    }

    @Test
    public void testFindReservationByItemIdOnNotFound() {
        // given
        Integer itemId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .build();
        // when
        List<ReservationComposeModel> reservations = service.findReservationByCondition(cond);
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    public void testFindReservationByItemIdAndFromDate() {
        // given
        Integer itemId = 3;
        LocalDate fromDate = LocalDate.of(2020, 4, 1);
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        // when
        List<ReservationComposeModel> reservations = service.findReservationByCondition(cond);
        // then
        assertThatByEqualable(reservations).containsExactly(model1, model2);
    }

    @Test
    public void testFindReservationByItemIdAndFromDateOnNotFound() {
        // given
        Integer itemId = 3;
        LocalDate fromDate = LocalDate.of(2019, 4, 1);
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        // when
        List<ReservationComposeModel> reservations = service.findReservationByCondition(cond);
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    public void testFindReservationByReserverId() {
        // given
        Integer reserverId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(reserverId)
                .build();
        // when
        List<ReservationComposeModel> reservations = service.findReservationByCondition(cond);
        // then
        assertThatByEqualable(reservations).containsExactly(model1, model3);
    }

    @Test
    public void testFindReservationByReserverIdOnNotFound() {
        // given
        Integer reserverId = 9;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(reserverId)
                .build();
        // when
        List<ReservationComposeModel> reservations = service.findReservationByCondition(cond);
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    public void testGetOwnReservations() {
        // given
        int reserverId = 1;
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");

        // when
        List<ReservationComposeModel> reservations = service.getOwnReservations();
        // then
        assertThatByEqualable(reservations).containsExactly(model1, model3);
    }

    @Test
    public void testGetOwnReservationsOnNotFound() {
        // given
        int reserverId = 3;
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");

        // when
        List<ReservationComposeModel> reservations = service.getOwnReservations();
        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    public void testReserve(@Autowired ReservationRepository forResultAssert) {
        // given
        int reserverId = 1;
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)))
                .note("note")
                .itemId(new ItemId(1))
                .build();
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");

        // when
        ReservationComposeModel actual = service.reserve(command);

        // then
        int addedId = forResultAssert.nextIdentity().id() - 1;
        Reservation added = testCreator.newInstance(
                new ReservationId(addedId),
                command.period(),
                command.note(),
                command.itemId(),
                new UserId(reserverId));
        ReservationComposeModel expected = new ReservationComposeModel(added, item1, user1);
        ToStringAssert.assertThatToString(actual).isEqualTo(expected);
        // commitされているか確認するため読み返し
        Optional<Reservation> persisted = forResultAssert.find(added.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(added);
    }

    @Test
    public void testReserveOnDuplicate(@Autowired ReservationRepository forResultAssert) {

        // -- 事前条件
        int reserverId = 1;
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");
        LocalDateTime from = LocalDateTime.now().plusHours(1);
        LocalDateTime to = from.plusHours(2);
        ReserveItemCommand preCommand = ReserveItemCommand.builder()
                .period(new ReservationPeriod(from, to))
                .note("note")
                .itemId(new ItemId(3))
                .build();
        ReservationComposeModel preCondition = service.reserve(preCommand);

        // given
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        preCondition.reservation().getPeriod().getFrom().plusHours(1),
                        preCondition.reservation().getPeriod().getTo().plusHours(1)))
                .note("note")
                .itemId(new ItemId(3))
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
    public void testReserveOnValidationErrorOfProperty(@Autowired ReservationRepository forResultAssert) {
        // given
        TestAuthUtils.signinByHeaderWithRolePrefix(1, "MEMBER");
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().minusDays(1), // 過去日付エラー
                        LocalDateTime.now()))
                .note("note")
                .itemId(new ItemId(1))
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
    public void testReserveOnItemNotExist(@Autowired ReservationRepository forResultAssert) {
        // given
        TestAuthUtils.signinByHeaderWithRolePrefix(1, "MEMBER");
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)))
                .note("note")
                .itemId(new ItemId(999)) // unknown item
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
    public void testReserveOnUserNotExist(@Autowired ReservationRepository forResultAssert) {
        // given
        int reserverId = 999;  // unknown user
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(new ReservationPeriod(
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)))
                .note("note")
                .itemId(new ItemId(1))
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
    public void testCancel(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId cancelId = reservation3.getId();
        int reserverId = 1;
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");

        // when
        service.cancel(cancelId);

        // then
        Optional<Reservation> persisted = forResultAssert.find(cancelId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    public void testCancelOnOthreUserReservation(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId cancelId = reservation1.getId();
        int reserverId = 3;
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");

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
    public void testCancelOnNotFound(@Autowired ReservationRepository forResultAssert) {
        // given
        ReservationId cancelId = new ReservationId(99);
        int reserverId = 1;
        TestAuthUtils.signinByHeaderWithRolePrefix(reserverId, "MEMBER");

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.cancel(cancelId);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }
}
