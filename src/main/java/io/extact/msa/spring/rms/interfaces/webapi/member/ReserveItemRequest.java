package io.extact.msa.spring.rms.interfaces.webapi.member;

import java.time.LocalDateTime;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.member.ReserveItemCommand;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTimeFuture;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import lombok.Builder;

/**
 * レンタル商品予約リクエスト。
 * 予約者(reserverId)はログインユーザが補完されるため属性を持っていない。
 */
@Builder
@BeforeAfterDateTime
record ReserveItemRequest(
        @FromDateTime @FromDateTimeFuture LocalDateTime fromDateTime,
        @ToDateTime LocalDateTime toDateTime,
        @Note String note,
        @RmsId Integer itemId) implements Transformable, BeforeAfterDateTimeValidatable {

    ReserveItemCommand toCommand() {
        return ReserveItemCommand.builder()
                .period(new ReservationPeriod(fromDateTime, toDateTime))
                .note(this.note)
                .itemId(new ItemId(this.itemId))
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
