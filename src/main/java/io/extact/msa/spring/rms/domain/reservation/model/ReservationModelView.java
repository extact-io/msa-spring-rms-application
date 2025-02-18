package io.extact.msa.spring.rms.domain.reservation.model;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public interface ReservationModelView extends EntityModelView {

    ReservationId getId();
    ReservationPeriod getPeriod();
    String getNote();

    UserId getReserverId();
    ItemId getItemId();
}