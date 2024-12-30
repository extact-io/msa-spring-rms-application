package io.extact.msa.spring.rms.application.support;

import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationReference;
import io.extact.msa.spring.rms.domain.user.model.UserReference;

public record ReservationComposeModel(
        ReservationReference reservation,
        ItemReference rentalItem,
        UserReference reserver) implements Transformable {
}
