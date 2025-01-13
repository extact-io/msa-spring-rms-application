package io.extact.msa.spring.rms.interfaces.webapi.member;

import java.time.LocalDateTime;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.member.ReserveItemCommand;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;

@Builder
@BeforeAfterDateTime
record ReserveItemRequest(
        @RmsId int id,
        @FromDateTime LocalDateTime fromDateTime,
        @ToDateTime LocalDateTime toDateTime,
        @Note String note,
        @RmsId int itemId,
        @RmsId int reserverId) implements Transformable, BeforeAfterDateTimeValidatable {

    ReserveItemCommand toCommand() {
        return ReserveItemCommand.builder()
                .period(new ReservationPeriod(fromDateTime, toDateTime))
                .note(this.note)
                .itemId(new ItemId(this.id))
                .reserverId(new UserId(this.reserverId))
                .build();
    }

    @Override
    public LocalDateTime getFrom() {
        return fromDateTime;
    }

    @Override
    public LocalDateTime getTo() {
        return toDateTime;
    }
}
