package io.extact.msa.spring.rms.application.support;

import io.extact.msa.spring.platform.core.generic.Transformable;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.user.model.UserModelView;

public record ReservationComposeModel(
        ReservationModelView reservation,
        ItemModelView rentalItem,
        UserModelView reserver) implements Transformable {
}
