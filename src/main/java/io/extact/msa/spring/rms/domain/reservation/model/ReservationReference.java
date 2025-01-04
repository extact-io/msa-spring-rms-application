package io.extact.msa.spring.rms.domain.reservation.model;

import java.time.LocalDateTime;

import io.extact.msa.spring.platform.fw.domain.model.ReferenceModel;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.constraint.DateTimePeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public interface ReservationReference extends ReferenceModel {

    ReservationId getId();
    LocalDateTime getFromDateTime();
    LocalDateTime getToDateTime();
    DateTimePeriod getReservePeriod();
    String getNote();

    UserId getReserverId();
    ItemId getItemId();
}