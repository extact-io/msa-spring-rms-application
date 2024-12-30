package io.extact.msa.spring.rms.interfaces.webapi.admin;

import io.extact.msa.spring.platform.fw.domain.constraint.Contact;
import io.extact.msa.spring.platform.fw.domain.constraint.LoginId;
import io.extact.msa.spring.platform.fw.domain.constraint.Passowrd;
import io.extact.msa.spring.platform.fw.domain.constraint.PhoneNumber;
import io.extact.msa.spring.platform.fw.domain.constraint.UserName;
import io.extact.msa.spring.platform.fw.domain.constraint.UserTypeConstraint;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.platform.fw.domain.type.UserType;
import io.extact.msa.spring.rms.application.admin.UserAddCommand;
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
