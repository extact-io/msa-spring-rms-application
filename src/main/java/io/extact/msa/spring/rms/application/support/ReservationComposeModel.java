package io.extact.msa.spring.rms.application.support;

import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.user.model.User;

public record ReservationComposeModel(
        Reservation reservation,
        Item rentalItem,
        User reserver) {
}
