package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationSpecification {

    public static Specification<ReservationEntity> fromCondition(ReserveItemQueryCondition condition) {

        return (root, _, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            condition.getItemIdAsOptional().ifPresent(itemId -> {
                predicates.add(cb.equal(root.get("itemId"), itemId));
            });

            condition.getReserverIdAsOptional().ifPresent(reserverId -> {
                predicates.add(cb.equal(root.get("reserverId"), reserverId));
            });

            condition.getFromAsOptional().ifPresent(from -> {
                LocalDateTime fromStart = from.atStartOfDay();
                LocalDateTime fromEnd = from.atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("fromDateTime"), fromEnd));
                predicates.add(cb.greaterThanOrEqualTo(root.get("toDateTime"), fromStart));
            });

            return cb.and(predicates.stream().toArray(Predicate[]::new));
        };
    }
}
