package io.extact.msa.spring.rms.application.member;

import java.time.LocalDate;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;

@Builder
public record ReservationSearchCondition(
		LocalDate from,
		ItemId itemId,
        UserId reserverId) {
}
