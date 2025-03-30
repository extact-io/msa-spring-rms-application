package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import java.time.LocalDateTime;

import io.extact.msa.spring.rms.application.support.ReservationComposeModel;

public record RemoteReservation(
        int id,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime,
        String note,
        int itemId,
        int reserverId) {

    static RemoteReservation from(ReservationComposeModel model) {
        if (model == null) {
            return null;
        }
        return new RemoteReservation(
                model.reservation().getId().id(),
                model.reservation().getPeriod().getFrom(),
                model.reservation().getPeriod().getTo(),
                model.reservation().getNote(),
                model.reservation().getItemId().id(),
                model.reservation().getReserverId().id());
    }
}
