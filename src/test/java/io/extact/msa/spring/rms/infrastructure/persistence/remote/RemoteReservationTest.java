package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservation;

class RemoteReservationTest {

    private static final ReservationCreatable testCreater = new ReservationCreatable() {};

    @Test
    void testConstructor() {
        // given
        Integer id = 1;
        LocalDateTime fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime toDateTime = LocalDateTime.of(2025, 1, 1, 12, 0);
        String note = "Meeting";
        int itemId = 101;
        int reserverId = 202;

        // when
        RemoteReservation remoteReservation = new RemoteReservation(
                id, fromDateTime, toDateTime, note, itemId, reserverId);

        // then
        assertThat(remoteReservation).isNotNull();
        assertThat(remoteReservation.id()).isEqualTo(id);
        assertThat(remoteReservation.fromDateTime()).isEqualTo(fromDateTime);
        assertThat(remoteReservation.toDateTime()).isEqualTo(toDateTime);
        assertThat(remoteReservation.note()).isEqualTo(note);
        assertThat(remoteReservation.itemId()).isEqualTo(itemId);
        assertThat(remoteReservation.reserverId()).isEqualTo(reserverId);
    }

    @Test
    void testFromReservation() {
        // given
        Reservation reservation = testCreater.newInstance(
                new ReservationId(1),
                new ReservationPeriod(
                        LocalDateTime.of(2025, 1, 1, 10, 0),
                        LocalDateTime.of(2025, 1, 1, 12, 0)),
                "Meeting",
                new ItemId(101),
                new UserId(202));

        // when
        RemoteReservation remoteReservation = RemoteReservation.from(reservation);

        // then
        assertThat(remoteReservation).isNotNull();
        assertThat(remoteReservation.id()).isEqualTo(1);
        assertThat(remoteReservation.fromDateTime()).isEqualTo(reservation.getPeriod().getFrom());
        assertThat(remoteReservation.toDateTime()).isEqualTo(reservation.getPeriod().getTo());
        assertThat(remoteReservation.note()).isEqualTo(reservation.getNote());
        assertThat(remoteReservation.itemId()).isEqualTo(reservation.getItemId().id());
        assertThat(remoteReservation.reserverId()).isEqualTo(reservation.getReserverId().id());
    }

    @Test
    void testToModel() {
        // given
        RemoteReservation remoteReservation = new RemoteReservation(
                1,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0),
                "Meeting",
                101,
                202);

        // when
        Reservation reservation = remoteReservation.toModel(null);

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getId().id()).isEqualTo(1);
        assertThat(reservation.getPeriod().getFrom()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(reservation.getPeriod().getTo()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(reservation.getNote()).isEqualTo("Meeting");
        assertThat(reservation.getItemId().id()).isEqualTo(101);
        assertThat(reservation.getReserverId().id()).isEqualTo(202);
    }
}
