package io.extact.msa.spring.rms.webapi.universal;

import io.extact.msa.spring.rms.domain.user.model.UserModelView;
import io.extact.msa.spring.rms.domain.user.model.UserType;

public record UserProfileResponse(
        int id,
        String loginId,
        String password,
        UserType userType,
        String userName,
        String phoneNumber,
        String contact) {

    static UserProfileResponse from(UserModelView user) {
        if (user == null) {
            return null;
        }
        return new UserProfileResponse(
                user.getId().id(),
                user.getLoginId(),
                user.getPassword(),
                user.getUserType(),
                user.getProfile().getUserName(),
                user.getProfile().getPhoneNumber(),
                user.getProfile().getContact());
    }
}
