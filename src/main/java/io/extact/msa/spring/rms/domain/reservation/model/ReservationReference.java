package io.extact.msa.spring.rms.domain.reservation.model;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelReference;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public interface ReservationReference extends EntityModelReference {

    ReservationId getId();
    ReservationPeriod getPeriod();
    String getNote();

    UserId getReserverId();
    ItemId getItemId();
}