package io.extact.msa.spring.rms.application.admin;

import lombok.Builder;

@Builder
public record AddItemCommand(
        String serialNo,
        String itemName) {
}
