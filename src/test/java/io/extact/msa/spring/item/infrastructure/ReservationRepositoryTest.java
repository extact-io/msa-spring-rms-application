package io.extact.msa.spring.item.infrastructure;

import static io.extact.msa.spring.test.assertj.ToStringAssert.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import io.extact.msa.spring.platform.fw.exception.RmsPersistenceException;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;

@Transactional
@Rollback
public abstract class ReservationRepositoryTest {

    protected static final ReservationCreatable testCreator = new ReservationCreatable() {
    };

    private static final Reservation reservation1 = testCreator
            .newInstance(
                    new ReservationId(1),
                    LocalDateTime.of(2020, 4, 1, 10, 0),
                    LocalDateTime.of(2020, 4, 1, 12, 0),
                    "メモ1",
                    new ItemId(3),
                    new UserId(1));
    private static final Reservation reservation2 = testCreator
            .newInstance(
                    new ReservationId(2),
                    LocalDateTime.of(2020, 4, 1, 16, 0),
                    LocalDateTime.of(2020, 4, 1, 18, 0),
                    "メモ2",
                    new ItemId(3),
                    new UserId(2));
    private static final Reservation reservation3 = testCreator
            .newInstance(
                    new ReservationId(3),
                    LocalDateTime.of(2099, 4, 1, 10, 0),
                    LocalDateTime.of(2099, 4, 1, 12, 0),
                    "メモ3",
                    new ItemId(3),
                    new UserId(1));

    protected abstract ReservationRepository repository();

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
                        LocalDateTime.of(2024, 10, 30, 10, 0),
                        LocalDateTime.of(2025, 11, 1, 12, 0),
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
                        LocalDateTime.of(2024, 10, 30, 10, 0),
                        LocalDateTime.of(2025, 11, 1, 12, 0),
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
        Reservation addReservation = testCreator
                .newInstance(
                        new ReservationId(4),
                        LocalDateTime.of(2024, 10, 30, 10, 0),
                        LocalDateTime.of(2025, 11, 1, 12, 0),
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
        Reservation deleteReservation = testCreator
                .newInstance(
                        new ReservationId(3),
                        LocalDateTime.of(2099, 4, 1, 10, 0),
                        LocalDateTime.of(2099, 4, 1, 12, 0),
                        "メモ3",
                        new ItemId(3),
                        new UserId(1));
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
                        LocalDateTime.of(2099, 4, 1, 10, 0),
                        LocalDateTime.of(2099, 4, 1, 12, 0),
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
}
