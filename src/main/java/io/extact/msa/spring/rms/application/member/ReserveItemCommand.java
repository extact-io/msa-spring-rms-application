package io.extact.msa.spring.rms.application.member;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;

@Builder
public record ReserveItemCommand(
        ReservationPeriod period,
        String note,
        ItemId itemId,
        UserId reserverId) {
}
