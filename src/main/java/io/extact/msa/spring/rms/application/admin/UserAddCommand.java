package io.extact.msa.spring.rms.application.admin;

import io.extact.msa.spring.platform.fw.domain.type.UserType;
import lombok.Builder;

@Builder
public record UserAddCommand(
        String loginId,
        String password,
        UserType userType,
        String userName,
        String phoneNumber,
        String contact) {
}
