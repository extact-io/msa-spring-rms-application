package io.extact.msa.spring.rms.domain.reservation.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.extact.msa.spring.platform.fw.domain.model.EntityModel;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
public class Reservation implements EntityModel, ReservationReference {

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

    private ModelValidator validator;

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

    public boolean isOverlappedBy(ReservationPeriod otherPeriod) {
        return this.getPeriod().isOverlappedBy(otherPeriod);
    }

    public void editReservation(ReservationPeriod period, String note) {
        this.setReservationPeriod(period);
        this.setNote(note);
    }

    @Override
    public void configureValidator(ModelValidator validator) {
        this.validator = validator;
    }

    private void setReservationPeriod(ReservationPeriod newValue) {
        Reservation test = new Reservation();
        test.period = newValue;
        validator.validateField(test, "period");

        this.period = newValue;
    }

    private void setNote(String newValue) {
        Reservation test = new Reservation();
        test.note = newValue;
        validator.validateField(test, "note");

        this.note = newValue;
    }

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
