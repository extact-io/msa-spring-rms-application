package io.extact.msa.spring.rms.application.admin;

import io.extact.msa.spring.rms.domain.user.model.UserType;
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
