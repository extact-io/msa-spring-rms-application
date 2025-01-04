package io.extact.msa.spring.rms.interfaces.webapi.admin;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.admin.UserUpdateCommand;
import io.extact.msa.spring.rms.domain.user.constraints.Contact;
import io.extact.msa.spring.rms.domain.user.constraints.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraints.PhoneNumber;
import io.extact.msa.spring.rms.domain.user.constraints.UserName;
import io.extact.msa.spring.rms.domain.user.constraints.UserTypeConstraint;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import lombok.Builder;

@Builder
record UserUpdateRequest(
        @RmsId Integer id,
        @Passowrd String password,
        @UserTypeConstraint UserType userType,
        @UserName String userName,
        @PhoneNumber String phoneNumber,
        @Contact String contact) implements Transformable {

    UserUpdateCommand toCommand() {
        return UserUpdateCommand.builder()
                .id(new UserId(this.id))
                .password(this.password)
                .userType(this.userType)
                .userName(this.userName)
                .phoneNumber(this.phoneNumber)
                .contact(this.contact)
                .build();
    }
}
