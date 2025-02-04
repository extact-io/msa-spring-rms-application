package io.extact.msa.spring.rms.interfaces.webapi.universal;

import java.util.Set;

import io.extact.msa.spring.platform.core.jwt.encode.UserClaims;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.domain.user.model.UserType;

public record LoginUserResponse(
        int id,
        String loginId,
        String password,
        UserType userType,
        String userName,
        String phoneNumber,
        String contact) implements UserClaims {

    static LoginUserResponse from(UserReference user) {
        if (user == null) {
            return null;
        }
        return new LoginUserResponse(
                user.getId().id(),
                user.getLoginId(),
                user.getPassword(),
                user.getUserType(),
                user.getProfile().getUserName(),
                user.getProfile().getPhoneNumber(),
                user.getProfile().getContact());
    }

    @Override
    public String userId() {
        return String.valueOf(id);
    }

    @Override
    public String principalName() {
        return loginId;
    }

    @Override
    public Set<String> groups() {
        return Set.of(userType.name());
    }
}
