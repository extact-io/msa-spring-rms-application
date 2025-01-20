package io.extact.msa.spring.rms.application.admin;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;

class ReservationUpdateCommandTest {

    @Test
    void testBuilder() {
        // given
        ReservationId id = new ReservationId(1);
        ReservationPeriod period = new ReservationPeriod(
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0));
        String note = "Test Note";

        // when
        ReservationUpdateCommand command = ReservationUpdateCommand.builder()
                .id(id)
                .period(period)
                .note(note)
                .build();

        // then
        assertThat(command.id()).isEqualTo(id);
        assertThat(command.period()).isEqualTo(period);
        assertThat(command.note()).isEqualTo(note);
    }
}
