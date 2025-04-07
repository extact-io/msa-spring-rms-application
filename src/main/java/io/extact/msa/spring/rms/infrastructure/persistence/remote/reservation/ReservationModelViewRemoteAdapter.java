package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class ReservationModelViewRemoteAdapter implements ReservationModelView {
    
    private final RemoteReservation adaptee;

    @ToString.Include
    @Override
    public ReservationId getId() {
        return new ReservationId(adaptee.id());
    }

    @ToString.Include
    @Override
    public ReservationPeriod getPeriod() {
        return new ReservationPeriod(adaptee.fromDateTime(), adaptee.toDateTime());
    }

    @ToString.Include
    @Override
    public String getNote() {
        return adaptee.note();
    }

    @ToString.Include
    @Override
    public UserId getReserverId() {
        return new UserId(adaptee.reserverId());
    }

    @ToString.Include
    @Override
    public ItemId getItemId() {
        return new ItemId(adaptee.itemId());
    }
}
