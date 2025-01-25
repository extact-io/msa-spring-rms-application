package io.extact.msa.spring.rms.interfaces.webapi.admin;

import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.domain.user.model.UserType;

public record UserAdminResponse(
        int id,
        String loginId,
        String password,
        UserType userType,
        String userName,
        String phoneNumber,
        String contact) {

    static UserAdminResponse from(UserReference user) {
        if (user == null) {
            return null;
        }
        return new UserAdminResponse(
                user.getId().id(),
                user.getLoginId(),
                user.getPassword(),
                user.getUserType(),
                user.getProfile().getUserName(),
                user.getProfile().getPhoneNumber(),
                user.getProfile().getContact());
    }
}
