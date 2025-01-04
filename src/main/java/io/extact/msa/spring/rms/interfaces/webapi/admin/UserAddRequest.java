package io.extact.msa.spring.rms.interfaces.webapi.admin;

import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.admin.UserAddCommand;
import io.extact.msa.spring.rms.domain.user.constraints.Contact;
import io.extact.msa.spring.rms.domain.user.constraints.LoginId;
import io.extact.msa.spring.rms.domain.user.constraints.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraints.PhoneNumber;
import io.extact.msa.spring.rms.domain.user.constraints.UserName;
import io.extact.msa.spring.rms.domain.user.constraints.UserTypeConstraint;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import lombok.Builder;

@Builder
record UserAddRequest(
        @LoginId String loginId,
        @Passowrd String password,
        @UserTypeConstraint UserType userType,
        @UserName String userName,
        @PhoneNumber String phoneNumber,
        @Contact String contact) implements Transformable {

    UserAddCommand toCommand() {
        return UserAddCommand.builder()
                .loginId(this.loginId)
                .password(this.password)
                .userType(this.userType)
                .userName(this.userName)
                .phoneNumber(this.phoneNumber)
                .contact(this.contact)
                .build();
    }
}
