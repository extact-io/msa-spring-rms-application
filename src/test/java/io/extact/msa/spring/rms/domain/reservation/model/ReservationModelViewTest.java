package io.extact.msa.spring.rms.domain.reservation.model;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;

class ReservationModelViewTest {

    private static final ReservationCreatable testCreater = new ReservationCreatable() {
    };

    @Test
    void testIsEqualNull() {
        assertThat(reservation1.isEqual(null)).isFalse();
    }

    @Test
    void testIsEqual() {
        assertThat(reservation1.isEqual(testCreater.newInstance(
                null,
                null,
                null,
                null,
                null)))
        .isFalse();
        assertThat(reservation1.isEqual(testCreater.newInstance(
                reservation1.getId(),
                null,
                null,
                null,
                null)))
        .isFalse();
        assertThat(reservation1.isEqual(testCreater.newInstance(
                reservation1.getId(),
                reservation1.getPeriod(),
                null,
                null,
                null)))
        .isFalse();
        assertThat(reservation1.isEqual(testCreater.newInstance(
                reservation1.getId(),
                reservation1.getPeriod(),
                reservation1.getNote(),
                null,
                null)))
        .isFalse();
        assertThat(reservation1.isEqual(testCreater.newInstance(
                reservation1.getId(),
                reservation1.getPeriod(),
                reservation1.getNote(),
                reservation1.getItemId(),
                null)))
        .isFalse();
        assertThat(reservation1.isEqual(testCreater.newInstance(
                reservation1.getId(),
                reservation1.getPeriod(),
                reservation1.getNote(),
                reservation1.getItemId(),
                reservation1.getReserverId())))
        .isTrue();
    }
}
