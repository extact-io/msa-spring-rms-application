package io.extact.msa.spring.rms.application.support;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReservationComposeModelTest {

    private static final ReservationComposeModel model1 = new ReservationComposeModel(reservation1, item1, user1);

    @Test
    void testIsEqualNull() {
        assertThat(model1.isEqual(null)).isFalse();
    }

    @Test
    void testIsEqual() {
        assertThat(model1.isEqual(new ReservationComposeModel(
                null,
                null,
                null //
                ))).isFalse();
        assertThat(model1.isEqual(new ReservationComposeModel(
                reservation1,
                null,
                null //
                ))).isFalse();
        assertThat(model1.isEqual(new ReservationComposeModel(
                reservation1,
                item1,
                null //
                ))).isFalse();
        assertThat(model1.isEqual(new ReservationComposeModel(
                reservation1,
                item1,
                user1 //
                ))).isTrue();
    }
}
