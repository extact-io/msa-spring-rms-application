package io.extact.msa.spring.rms.application.member;

import java.time.LocalDateTime;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;

@Builder
public record ReserveItemCommand(
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime,
        String note,
        ItemId itemId,
        UserId reserverId) {
}
