package io.extact.msa.spring.rms.infrastructure.persistence;

import static io.extact.msa.spring.platform.fw.test.utils.IsEqualableAssert.*;
import static io.extact.msa.spring.test.assertj.ToStringAssert.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import io.extact.msa.spring.platform.fw.feature.exception.RmsPersistenceException;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;

@Transactional
@Rollback
public abstract class AbstractReservationRepositoryTest {

    protected static final ReservationCreatable testCreator = new ReservationCreatable() {
    };

    private static final Reservation reservation1 = testCreator
            .newInstance(
                    new ReservationId(1),
                    new ReservationPeriod(
                            LocalDateTime.of(2020, 4, 1, 10, 0),
                            LocalDateTime.of(2020, 4, 1, 12, 0)),
                    "メモ1",
                    new ItemId(3),
                    new UserId(1));
    private static final Reservation reservation2 = testCreator
            .newInstance(
                    new ReservationId(2),
                    new ReservationPeriod(
                            LocalDateTime.of(2020, 4, 1, 16, 0),
                            LocalDateTime.of(2020, 4, 1, 18, 0)),
                    "メモ2",
                    new ItemId(3),
                    new UserId(2));
    private static final Reservation reservation3 = testCreator
            .newInstance(
                    new ReservationId(3),
                    new ReservationPeriod(
                            LocalDateTime.of(2099, 4, 1, 10, 0),
                            LocalDateTime.of(2099, 4, 1, 12, 0)),
                    "メモ3",
                    new ItemId(3),
                    new UserId(1));

    protected abstract ReservationRepository repository();
    protected abstract ReserveItemQueryService queryService();

    @Test
    void testGet() {

        // given
        ReservationId foundId = new ReservationId(1);
        // when
        Optional<Reservation> actual = repository().find(foundId);
        // then
        assertThat(actual).isPresent();
        assertThatToString(actual.get()).isEqualTo(reservation1);

        // given
        ReservationId notFoundId = new ReservationId(999);
        // when
        actual = repository().find(notFoundId);
        // then
        assertThat(actual).isNotPresent();
    }

    @Test
    void testGetAll() {
        // given
        List<Reservation> expected = List.of(reservation1, reservation2, reservation3);
        // when
        List<Reservation> actual = repository().findAll();
        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testUpdate() {
        // given
        Reservation updateReservation = testCreator
                .newInstance(
                        new ReservationId(1),
                        new ReservationPeriod(
                                LocalDateTime.of(2024, 10, 30, 10, 0),
                                LocalDateTime.of(2025, 11, 1, 12, 0)),
                        "UPDATE",
                        new ItemId(1),
                        new UserId(3));
        // when
        repository().update(updateReservation);
        //then
        assertThatToString(repository().find(updateReservation.getId()).get()).isEqualTo(updateReservation);
    }

    @Test
    void testUpdateOnNotFound() {
        // given
        Reservation notFoundReservation = testCreator
                .newInstance(
                        new ReservationId(999),
                        new ReservationPeriod(
                                LocalDateTime.of(2024, 10, 30, 10, 0),
                                LocalDateTime.of(2025, 11, 1, 12, 0)),
                        "UPDATE",
                        new ItemId(1),
                        new UserId(3));

        // when & then
        RmsPersistenceException exception = assertThrows(RmsPersistenceException.class, () -> {
            repository().update(notFoundReservation);
        });
        assertThat(exception).hasMessageContaining("id:" + notFoundReservation.getId().id());
    }

    @Test
    void testAdd() {

        // given
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = from.plusHours(2);

        Reservation addReservation = testCreator
                .newInstance(
                        new ReservationId(4),
                        new ReservationPeriod(from, to),
                        "ADD",
                        new ItemId(1),
                        new UserId(3));

        // when
        repository().add(addReservation);

        // then
        assertThatToString(repository().find(addReservation.getId()).get()).isEqualTo(addReservation);
    }

    @Test
    void testDelete() {
        // given
        Reservation deleteReservation = reservation3;
        // when
        repository().delete(deleteReservation);
        // then
        assertThat(repository().find(deleteReservation.getId())).isEmpty();
    }

    @Test
    void testDeleteOnNotFound() {
        // given
        Reservation notFoundReservation = testCreator
                .newInstance(
                        new ReservationId(999),
                        new ReservationPeriod(
                                LocalDateTime.of(2099, 4, 1, 10, 0),
                                LocalDateTime.of(2099, 4, 1, 12, 0)),
                        "メモ3",
                        new ItemId(3),
                        new UserId(1));
        // when & then
        RmsPersistenceException exception = assertThrows(RmsPersistenceException.class, () -> {
            repository().delete(notFoundReservation);
        });
        assertThat(exception).hasMessageContaining("id:" + notFoundReservation.getId().id());
    }

    protected abstract void testNextIdentity();

    
    // ------ reservation unique spec

    @Test
    void testFindByItemId() {

        // ---- 3件ヒット
        // given
        ItemId itemId = new ItemId(3);
        // when
        List<Reservation> actual = repository().findByItemId(itemId);
        // then
        List<Reservation> expected = List.of(reservation1, reservation2, reservation3);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);

        // ---- 0件ヒット
        // given
        itemId = new ItemId(1);
        // when
        actual = repository().findByItemId(itemId);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByReserverId() {

        // ---- 1件ヒット
        // given
        UserId reserverId = new UserId(2);
        // when
        List<Reservation> actual = repository().findByReserverId(reserverId);
        // then
        List<Reservation> expected = List.of(reservation2);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);

        // ---- 2件ヒット
        // given
        reserverId = new UserId(1);
        // when
        actual = repository().findByReserverId(reserverId);
        // then
        expected = List.of(reservation1, reservation3);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);

        // ---- 0件ヒット
        // given
        reserverId = new UserId(3);
        // when
        actual = repository().findByReserverId(reserverId);
        // then
        assertThat(actual).isEmpty();
    }
    
    @Test
    public void testFindByConditionWithItemIdAndFromDate() {

        // ---- 1件ヒット
        // given
        Integer itemId = 3;
        LocalDate fromDate = LocalDate.of(2099, 4, 1);
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        
        // when
        List<ReservationModelView> actual = queryService().findByCondition(cond);

        // then
        List<ReservationModelView> expected = List.of(reservation3);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);

        // ---- 2件ヒット
        // given
        itemId = 3;
        fromDate = LocalDate.of(2020, 4, 1);
        cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();

        // when
        actual = queryService().findByCondition(cond);

        // then
        expected = List.of(reservation1, reservation2);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testFindByConditionWithItemIdAndFromDateOnNotFound() {

        // ---- 開始日に該当なし
        // given
        Integer itemId = 3;
        LocalDate fromDate = LocalDate.of(2999, 4, 1);
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        
        // when
        List<ReservationModelView> actual = queryService().findByCondition(cond);
        
        // then
        assertThat(actual).isEmpty();

        // ---- レンタル品に該当なし
        // given
        itemId = 999;
        fromDate = LocalDate.of(2020, 4, 1);
        cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        
        // when
        actual = queryService().findByCondition(cond);
        
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByConditionWithReserverId() {

        // ---- 1件ヒット
        // given
        Integer reserverId = 2;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition
                .builder()
                .reserverId(reserverId)
                .build();
        // when
        List<ReservationModelView> actual = queryService().findByCondition(cond);
        // then
        List<ReservationModelView> expected = List.of(reservation2);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);

        // ---- 2件ヒット
        // given
        reserverId = 1;
        cond = ReserveItemQueryCondition
                .builder()
                .reserverId(reserverId)
                .build();
        // when
        actual = queryService().findByCondition(cond);
        // then
        expected = List.of(reservation1, reservation3);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);

        // ---- 0件ヒット
        // given
        reserverId = 3;
        cond = ReserveItemQueryCondition
                .builder()
                .reserverId(reserverId)
                .build();
        // when
        actual = queryService().findByCondition(cond);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByConditionWithItemId() {

        // ---- 3件ヒット
        // given
        Integer itemId = 3;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition
                .builder()
                .itemId(itemId)
                .build();
        // when
        List<ReservationModelView> actual = queryService().findByCondition(cond);
        // then
        List<ReservationModelView> expected = List.of(reservation1, reservation2, reservation3);
        assertThatByEqualable(actual).containsExactlyElementsOf(expected);

        // ---- 0件ヒット
        // given
        itemId = 1;
        cond = ReserveItemQueryCondition
                .builder()
                .itemId(itemId)
                .build();
        // when
        actual = queryService().findByCondition(cond);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindOverlappingReservations() {

        // --- 指定開始時間が既存予約時間帯に完全に含まれる
        // given
        List<Reservation> expected = List.of(reservation1);
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 11, 00);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 14, 00);
        // when
        List<Reservation> actual = repository().findOverlappingReservations(new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 指定終了時間が既存予約時間帯に含まれるケース
        // given
        expected = List.of(reservation1);
        from = LocalDateTime.of(2020, 4, 1, 9, 00);
        to = LocalDateTime.of(2020, 4, 1, 11, 00);
        // when
        actual = repository().findOverlappingReservations(new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 指定時間帯が既存の予約時間帯を包含するケース
        // given
        expected = List.of(reservation1);
        from = LocalDateTime.of(2020, 4, 1, 9, 00);
        to = LocalDateTime.of(2020, 4, 1, 14, 00);
        // when
        actual = repository().findOverlappingReservations(new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 指定時間帯が既存の予約時間帯に包含されるケース
        // given
        expected = List.of(reservation1);
        from = LocalDateTime.of(2020, 4, 1, 10, 30);
        to = LocalDateTime.of(2020, 4, 1, 11, 30);
        // when
        actual = repository().findOverlappingReservations(new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 複数件取得するケース
        // given
        expected = List.of(reservation1, reservation2, reservation3);
        from = LocalDateTime.of(2020, 4, 1, 0, 0);
        to = LocalDateTime.of(2099, 12, 31, 23, 59);
        // when
        actual = repository().findOverlappingReservations(new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testFindOverlappingReservationsOnNotFound() {

        // --- 時間帯が重複しないケース
        // given
        LocalDateTime from = LocalDateTime.of(2999, 4, 1, 13, 00);
        LocalDateTime to = LocalDateTime.of(2999, 4, 1, 15, 30);
        // when
        List<Reservation> actual = repository().findOverlappingReservations(new ReservationPeriod(from, to));
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindOverlappingReservationsWithItemId() {

        // --- 指定開始時間が既存予約時間帯に完全に含まれる
        // given
        List<Reservation> expected = List.of(reservation1);
        ItemId itemId = new ItemId(3);
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 11, 00);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 14, 00);
        // when
        List<Reservation> actual = repository().findOverlappingReservations(itemId, new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 指定終了時間が既存予約時間帯に含まれるケース
        // given
        expected = List.of(reservation1);
        itemId = new ItemId(3);
        from = LocalDateTime.of(2020, 4, 1, 9, 00);
        to = LocalDateTime.of(2020, 4, 1, 11, 00);
        // when
        actual = repository().findOverlappingReservations(itemId, new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 指定時間帯が既存の予約時間帯を包含するケース
        // given
        expected = List.of(reservation1);
        itemId = new ItemId(3);
        from = LocalDateTime.of(2020, 4, 1, 9, 00);
        to = LocalDateTime.of(2020, 4, 1, 14, 00);
        // when
        actual = repository().findOverlappingReservations(itemId, new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 指定時間帯が既存の予約時間帯に包含されるケース
        // given
        expected = List.of(reservation1);
        itemId = new ItemId(3);
        from = LocalDateTime.of(2020, 4, 1, 10, 30);
        to = LocalDateTime.of(2020, 4, 1, 11, 30);
        // when
        actual = repository().findOverlappingReservations(itemId, new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);

        // --- 複数件取得するケース
        // given
        expected = List.of(reservation1, reservation2, reservation3);
        itemId = new ItemId(3);
        from = LocalDateTime.of(2020, 4, 1, 0, 0);
        to = LocalDateTime.of(2099, 12, 31, 23, 59);
        // when
        actual = repository().findOverlappingReservations(itemId, new ReservationPeriod(from, to));
        // then
        assertThatToString(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testFindOverlappingReservationsWithItemIdOnNotFound() {

        // --- 指定開始時間が既存予約時間帯に完全に含まれる予約があるがレンタル品に該当なし
        // given
        ItemId itemId = new ItemId(1);
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 11, 00);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 14, 00);
        // when
        List<Reservation> actual = repository().findOverlappingReservations(itemId, new ReservationPeriod(from, to));
        // then
        assertThat(actual).isEmpty();
    }
}
