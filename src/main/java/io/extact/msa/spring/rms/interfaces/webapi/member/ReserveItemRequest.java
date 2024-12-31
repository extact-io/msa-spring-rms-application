package io.extact.msa.spring.rms.interfaces.webapi.member;

import java.time.LocalDateTime;

import io.extact.msa.spring.platform.fw.domain.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.platform.fw.domain.constraint.Note;
import io.extact.msa.spring.platform.fw.domain.constraint.ReserveFromDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.ReserveToDateTime;
import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.member.ReserveItemCommand;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;

@Builder
@BeforeAfterDateTime
record ReserveItemRequest(
        @RmsId int id,
        @ReserveFromDateTime LocalDateTime fromDateTime,
        @ReserveToDateTime LocalDateTime toDateTime,
        @Note String note,
        @RmsId int itemId,
        @RmsId int reserverId) implements Transformable, BeforeAfterDateTimeValidatable {

    ReserveItemCommand toCommand() {
        return ReserveItemCommand.builder()
                .fromDateTime(this.fromDateTime)
                .toDateTime(this.toDateTime)
                .note(this.note)
                .itemId(new ItemId(this.id))
                .reserverId(new UserId(this.reserverId))
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
