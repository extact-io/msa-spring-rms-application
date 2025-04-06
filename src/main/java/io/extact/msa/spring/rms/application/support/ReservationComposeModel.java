package io.extact.msa.spring.rms.application.support;

import io.extact.msa.spring.platform.core.generic.Transformable;
import io.extact.msa.spring.platform.fw.domain.model.IsEqualable;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.user.model.UserModelView;

public record ReservationComposeModel(
        ReservationModelView reservation,
        ItemModelView rentalItem,
        UserModelView reserver) implements Transformable, IsEqualable<ReservationComposeModel> {

    @Override
    public boolean isEqual(ReservationComposeModel other) {
        if (other == null) {
            return false;
        }
        return reservation.isEqual(other.reservation)
                && rentalItem.isEqual(other.rentalItem)
                && reserver.isEqual(other.reserver);
    }
}
