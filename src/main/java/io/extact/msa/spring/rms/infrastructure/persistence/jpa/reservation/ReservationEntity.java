package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import static jakarta.persistence.AccessType.*;

import java.time.LocalDateTime;

import jakarta.persistence.Access;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.TableEntity;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Access(FIELD)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ReservationEntity implements TableEntity<Reservation>, ReservationCreatable {

    @Id
    private Integer id;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime fromDateTime;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime toDateTime;
    private String note;

    private int itemId;
    private int reserverId;

    public static ReservationEntity from(Reservation model) {
        return new ReservationEntity(
                model.getId().id(),
                model.getFromDateTime(),
                model.getToDateTime(),
                model.getNote(),
                model.getItemId().id(),
                model.getReserverId().id());
    }

    @Override
    public Reservation toModel() {
        return newInstance(
                new ReservationId(id),
                fromDateTime,
                toDateTime,
                note,
                new ItemId(itemId),
                new UserId(reserverId));
    }
}
