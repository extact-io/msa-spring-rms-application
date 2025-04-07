package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class ReservationModelViewTableAdapter implements ReservationModelView {
    
    private final ReservationEntity adaptee;

    @ToString.Include
    @Override
    public ReservationId getId() {
        return new ReservationId(adaptee.getId());
    }

    @ToString.Include
    @Override
    public ReservationPeriod getPeriod() {
        return new ReservationPeriod(adaptee.getFromDateTime(), adaptee.getToDateTime());
    }

    @ToString.Include
    @Override
    public String getNote() {
        return adaptee.getNote();
    }

    @ToString.Include
    @Override
    public UserId getReserverId() {
        return new UserId(adaptee.getReserverId());
    }

    @ToString.Include
    @Override
    public ItemId getItemId() {
        return new ItemId(adaptee.getItemId());
    }
}
