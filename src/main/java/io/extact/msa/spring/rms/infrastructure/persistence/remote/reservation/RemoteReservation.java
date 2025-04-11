package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.PhysicalEntity;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;

@JsonIgnoreProperties("pk")
public record RemoteReservation(
        int id,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime,
        String note,
        int itemId,
        int reserverId) implements PhysicalEntity<Reservation>, ReservationCreatable {
    

    public static RemoteReservation from(Reservation model) {
        return new RemoteReservation(
                model.getId().id(),
                model.getPeriod().getFrom(),
                model.getPeriod().getTo(),
                model.getNote(),
                model.getItemId().id(),
                model.getReserverId().id());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Reservation toModel(ModelValidator validator) {
        Reservation r = newInstance(
                new ReservationId(id),
                new ReservationPeriod(fromDateTime, toDateTime),
                note,
                new ItemId(itemId),
                new UserId(reserverId));
        r.configure(validator);
        return r;
    }
}
