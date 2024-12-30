package io.extact.msa.spring.rms.interfaces.webapi.admin;

import io.extact.msa.spring.platform.fw.domain.constraint.ItemName;
import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.constraint.SerialNo;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.admin.ItemUpdateCommand;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import lombok.Builder;

@Builder
record ItemUpdateRequest(
        @RmsId Integer id,
        @SerialNo String serialNo,
        @ItemName String itemName) implements Transformable {

    ItemUpdateCommand toCommand() {
        return ItemUpdateCommand.builder()
                .id(new ItemId(this.id))
                .serialNo(this.serialNo)
                .itemName(this.itemName)
                .build();
    }
}
