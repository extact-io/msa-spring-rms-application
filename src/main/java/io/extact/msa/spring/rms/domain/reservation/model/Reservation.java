package io.extact.msa.spring.rms.domain.reservation.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.extact.msa.spring.platform.fw.domain.model.AbstractEntityModel;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString
public class Reservation extends AbstractEntityModel implements ReservationModelView {

    @NotNull
    @Valid
    @Getter
    private ReservationId id;
    @NotNull
    @Valid
    @Getter
    private ReservationPeriod period;
    @Note
    @Getter
    private String note;

    @NotNull
    @Valid
    @Getter
    private ItemId itemId;
    @NotNull
    @Valid
    @Getter
    private UserId reserverId;

    Reservation(
            ReservationId id,
            ReservationPeriod period,
            String note,
            ItemId itemId,
            UserId reserverId) {

        this.id = id;
        this.period = period;
        this.note = note;
        this.itemId = itemId;
        this.reserverId = reserverId;
    }

    // --------------------------------------- service methods

    public boolean isOverlappedBy(ReservationPeriod otherPeriod) {
        return this.getPeriod().isOverlappedBy(otherPeriod);
    }

    public void editReservation(ReservationPeriod newPeriod, String newNote) {
        applyPeriod(newPeriod);
        applyNote(newNote);
    }

    // --------------------------------------- private methods

    private void applyPeriod(ReservationPeriod newPeriod) {
        Reservation test = new Reservation();
        test.period = newPeriod;
        validator().validateField(test, test::getPeriod);
        this.period = newPeriod;
    }

    private void applyNote(String newNote) {
        Reservation test = new Reservation();
        test.note = newNote;
        validator().validateField(test, test::getNote);
        this.note = newNote;
    }

    // --------------------------------------- inner interface

    public interface ReservationCreatable {
        default Reservation newInstance(
                ReservationId id,
                ReservationPeriod period,
                String note,
                ItemId itemId,
                UserId reserverId) {
            return new Reservation(id, period, note, itemId, reserverId);
        }
    }
}
