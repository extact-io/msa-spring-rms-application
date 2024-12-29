package io.extact.msa.spring.rms.domain.reservation.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import io.extact.msa.spring.platform.fw.domain.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.platform.fw.domain.constraint.Note;
import io.extact.msa.spring.platform.fw.domain.constraint.ReserveFromDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.ReserveFromDateTimeFuture;
import io.extact.msa.spring.platform.fw.domain.constraint.ReserveToDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.constraint.ValidationGroups.Add;
import io.extact.msa.spring.platform.fw.domain.model.DomainModel;
import io.extact.msa.spring.platform.fw.domain.type.DateTimePeriod;
import io.extact.msa.spring.platform.fw.exception.RmsConstraintViolationException;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
@BeforeAfterDateTime
public class Reservation implements DomainModel, BeforeAfterDateTimeValidatable {

    @RmsId
    private ReservationId id;
    @ReserveFromDateTime
    @ReserveFromDateTimeFuture(groups = Add.class)
    private LocalDateTime fromDateTime;
    @ReserveToDateTime
    private LocalDateTime toDateTime;
    @Note
    private String note;

    @RmsId
    private ItemId itemId;
    @RmsId
    private UserId reserverId;

    private Validator validator;

    public Reservation(ReservationId id, LocalDateTime fromDateTime, LocalDateTime toDateTime, String note,
            ItemId itemId, UserId reserverId) {
        this.id = id;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
        this.note = note;
        this.itemId = itemId;
        this.reserverId = reserverId;
    }

    @Override
    public void configureValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void verify() {
        Set<ConstraintViolation<Reservation>> result = this.validator.validate(this, Default.class, Add.class);
        if (!result.isEmpty()) {
            throw new RmsConstraintViolationException("validation error.", new HashSet<>(result));
        }
    }

    public DateTimePeriod getReservePeriod() {
        return new DateTimePeriod(fromDateTime, toDateTime);
    }

    public boolean isOverlappedBy(DateTimePeriod otherPeriod) {
        return this.getReservePeriod().isOverlappedBy(otherPeriod);
    }

    public void editReservation(LocalDateTime from, LocalDateTime to, String note) {
        this.fromDateTime = from;
        this.toDateTime = to;
        this.note = note;
    }


    public interface ReservationCreatable {
        default Reservation newInstance(
                ReservationId id,
                LocalDateTime fromDataTime,
                LocalDateTime toDateTime,
                String note,
                ItemId itemId,
                UserId reserverId) {
            return new Reservation(id, toDateTime, toDateTime, note, itemId, reserverId);
        }
    }
}