package io.extact.msa.spring.rms.domain.reservation;

import java.time.LocalDateTime;

import jakarta.validation.Validator;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationCreator {

    private final ReservationRepository repository;
    private final Validator validator;
    private final ReservationCreatable constructorProxy = new ReservationCreatable() {};

    public Reservation create(ReservationModelAttributes attrs) {

        ReservationId id = new ReservationId(repository.nextIdentity());
        Reservation reservation = constructorProxy.newInstance(
                id,
                attrs.fromDateTime,
                attrs.toDateTime,
                attrs.note,
                attrs.itemId,
                attrs.reservedUserId);

        reservation.configureValidator(validator);
        reservation.verify();

        return reservation;
    }

    @Builder
    public static class ReservationModelAttributes {

        private LocalDateTime fromDateTime;
        private LocalDateTime toDateTime;
        private String note;

        private ItemId itemId;
        private UserId reservedUserId;
    }
}
