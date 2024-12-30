package io.extact.msa.spring.rms.application.admin;

import lombok.Builder;

@Builder
public record ItemAddCommand(
        String serialNo,
        String itemName) {
}
