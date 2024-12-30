package io.extact.msa.spring.rms.interfaces.webapi.admin;

import java.time.LocalDateTime;

import io.extact.msa.spring.platform.fw.domain.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.platform.fw.domain.constraint.Note;
import io.extact.msa.spring.platform.fw.domain.constraint.ReserveFromDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.ReserveToDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.admin.ReservationUpdateCommand;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import lombok.Builder;

@Builder
@BeforeAfterDateTime
record ReservationUpdateRequest(
        @RmsId int id,
        @ReserveFromDateTime LocalDateTime fromDateTime,
        @ReserveToDateTime LocalDateTime toDateTime,
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
