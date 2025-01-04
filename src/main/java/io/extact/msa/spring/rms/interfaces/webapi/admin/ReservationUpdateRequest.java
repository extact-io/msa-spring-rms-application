package io.extact.msa.spring.rms.interfaces.webapi.admin;

import java.time.LocalDateTime;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.admin.ReservationUpdateCommand;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import lombok.Builder;

@Builder
@BeforeAfterDateTime
record ReservationUpdateRequest(
        @RmsId int id,
        @FromDateTime LocalDateTime fromDateTime,
        @ToDateTime LocalDateTime toDateTime,
        @Note String note) implements Transformable, BeforeAfterDateTimeValidatable {

    ReservationUpdateCommand toCommand() {
        return ReservationUpdateCommand.builder()
                .id(new ReservationId(this.id))
                .fromDateTime(this.fromDateTime)
                .toDateTime(this.toDateTime)
                .note(this.note)
                .build();
    }

    @Override
    public LocalDateTime getFromDateTime() {
        return fromDateTime;
    }

    @Override
    public LocalDateTime getToDateTime() {
        return toDateTime;
    }
}
