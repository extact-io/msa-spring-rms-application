package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation.ReservationEntity;

class ReservationEntityTest {

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
        ReservationEntity reservationEntity = new ReservationEntity(
                id, fromDateTime, toDateTime, note, itemId, reserverId);

        // then
        assertThat(reservationEntity).isNotNull();
        assertThat(reservationEntity.getId()).isEqualTo(id);
        assertThat(reservationEntity.getFromDateTime()).isEqualTo(fromDateTime);
        assertThat(reservationEntity.getToDateTime()).isEqualTo(toDateTime);
        assertThat(reservationEntity.getNote()).isEqualTo(note);
        assertThat(reservationEntity.getItemId()).isEqualTo(itemId);
        assertThat(reservationEntity.getReserverId()).isEqualTo(reserverId);
    }

    @Test
    void testSetters() {
        // given
        ReservationEntity reservationEntity = new ReservationEntity();
        Integer id = 1;
        LocalDateTime fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime toDateTime = LocalDateTime.of(2025, 1, 1, 12, 0);
        String note = "Meeting";
        int itemId = 101;
        int reserverId = 202;

        // when
        reservationEntity.setId(id);
        reservationEntity.setFromDateTime(fromDateTime);
        reservationEntity.setToDateTime(toDateTime);
        reservationEntity.setNote(note);
        reservationEntity.setItemId(itemId);
        reservationEntity.setReserverId(reserverId);

        // then
        assertThat(reservationEntity.getId()).isEqualTo(id);
        assertThat(reservationEntity.getFromDateTime()).isEqualTo(fromDateTime);
        assertThat(reservationEntity.getToDateTime()).isEqualTo(toDateTime);
        assertThat(reservationEntity.getNote()).isEqualTo(note);
        assertThat(reservationEntity.getItemId()).isEqualTo(itemId);
        assertThat(reservationEntity.getReserverId()).isEqualTo(reserverId);
    }

    @Test
    void testFromReservation() {
        // given
        Reservation reservation = testCreater.newInstance(
                new ReservationId(1),
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0),
                "Meeting",
                new ItemId(101),
                new UserId(202));

        // when
        ReservationEntity reservationEntity = ReservationEntity.from(reservation);

        // then
        assertThat(reservationEntity).isNotNull();
        assertThat(reservationEntity.getId()).isEqualTo(1);
        assertThat(reservationEntity.getFromDateTime()).isEqualTo(reservation.getFromDateTime());
        assertThat(reservationEntity.getToDateTime()).isEqualTo(reservation.getToDateTime());
        assertThat(reservationEntity.getNote()).isEqualTo(reservation.getNote());
        assertThat(reservationEntity.getItemId()).isEqualTo(reservation.getItemId().id());
        assertThat(reservationEntity.getReserverId()).isEqualTo(reservation.getReserverId().id());
    }

    @Test
    void testToModel() {
        // given
        ReservationEntity reservationEntity = new ReservationEntity(
                1,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0),
                "Meeting",
                101,
                202);

        // when
        Reservation reservation = reservationEntity.toModel();

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getId().id()).isEqualTo(1);
        assertThat(reservation.getFromDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(reservation.getToDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(reservation.getNote()).isEqualTo("Meeting");
        assertThat(reservation.getItemId().id()).isEqualTo(101);
        assertThat(reservation.getReserverId().id()).isEqualTo(202);
    }
}
