package io.extact.msa.spring.rms.interfaces.webapi.admin;

import java.time.LocalDateTime;

import io.extact.msa.spring.rms.application.support.ReservationComposeModel;

public record ReservationAdminResponse(
        int id,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime,
        String note,
        int itemId,
        int reserverId,
        ItemAdminResponse itemResponse,
        UserAdminResponse reserverReseponse) {

    static ReservationAdminResponse from(ReservationComposeModel model) {
        if (model == null) {
            return null;
        }
        return new ReservationAdminResponse(
                model.reservation().getId().id(),
                model.reservation().getFromDateTime(),
                model.reservation().getToDateTime(),
                model.reservation().getNote(),
                model.reservation().getItemId().id(),
                model.reservation().getReserverId().id(),
                model.rentalItem().transform(ItemAdminResponse::from),
                model.reserver().transform(UserAdminResponse::from));
    }
}
