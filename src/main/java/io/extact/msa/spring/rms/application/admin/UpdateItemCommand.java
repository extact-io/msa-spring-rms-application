package io.extact.msa.spring.rms.application.admin;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import lombok.Builder;

@Builder
public record UpdateItemCommand(
        ItemId id,
        String serialNo,
        String itemName) {
}