package io.extact.msa.spring.rms.application.member;

import java.time.LocalDate;
import java.util.Optional;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;

@Builder
public record ReservationQueryCondition(
        LocalDate from,
        Integer itemId,
        Integer reserverId) {

    public Optional<LocalDate> getFromAsOptional() {
        return Optional.ofNullable(from);
    }

    public Optional<ItemId> getItemIdAsOptional() {
        return Optional
                .ofNullable(itemId)
                .map(ItemId::new);
    }

    public Optional<UserId> getReserverIdAsOptional() {
        return Optional
                .ofNullable(reserverId)
                .map(UserId::new);
    }

    public boolean hasAnyCondition() {
        return from != null || itemId != null || reserverId != null;
    }
}
