package io.extact.msa.spring.rms.interfaces.webapi.admin;

import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.admin.ItemAddCommand;
import io.extact.msa.spring.rms.domain.item.constraint.ItemName;
import io.extact.msa.spring.rms.domain.item.constraint.SerialNo;
import lombok.Builder;

@Builder
record ItemAddRequest(
        @SerialNo String serialNo,
        @ItemName String itemName) implements Transformable {

    ItemAddCommand toCommand() {
        return ItemAddCommand.builder()
                .serialNo(this.serialNo)
                .itemName(this.itemName)
                .build();
    }
}
