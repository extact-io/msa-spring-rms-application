package io.extact.msa.spring.rms.application.member;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;

class ReserveItemCommandTest {

    @Test
    void testBuilder() {
        // given
        ReservationPeriod period = new ReservationPeriod(
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0));
        String note = "Team meeting reservation";
        ItemId itemId = new ItemId(101);
        UserId reserverId = new UserId(1001);

        // when
        ReserveItemCommand command = ReserveItemCommand.builder()
                .period(period)
                .note(note)
                .itemId(itemId)
                .reserverId(reserverId)
                .build();

        // then
        assertThat(command.period()).isEqualTo(period);
        assertThat(command.note()).isEqualTo(note);
        assertThat(command.itemId()).isEqualTo(itemId);
        assertThat(command.reserverId()).isEqualTo(reserverId);
    }
}
