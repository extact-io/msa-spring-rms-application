package io.extact.msa.spring.rms.application.member;

import java.time.LocalDate;
import java.util.Optional;

import lombok.Builder;

@Builder
public record ReserveItemQueryCondition(
        LocalDate from,
        Integer itemId,
        Integer reserverId) {

    public Optional<LocalDate> getFromAsOptional() {
        return Optional.ofNullable(from);
    }

    public Optional<Integer> getItemIdAsOptional() {
        return Optional.ofNullable(itemId);
    }

    public Optional<Integer> getReserverIdAsOptional() {
        return Optional.ofNullable(reserverId);
    }

    public boolean hasAnyCondition() {
        return from != null || itemId != null || reserverId != null;
    }
}
