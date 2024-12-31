package io.extact.msa.spring.rms.interfaces.webapi.universal;

import io.extact.msa.spring.platform.fw.domain.constraint.Contact;
import io.extact.msa.spring.platform.fw.domain.constraint.Passowrd;
import io.extact.msa.spring.platform.fw.domain.constraint.PhoneNumber;
import io.extact.msa.spring.platform.fw.domain.constraint.UserName;
import io.extact.msa.spring.platform.fw.domain.model.Transformable;
import io.extact.msa.spring.rms.application.universal.UserProfileUpdateCommand;
import lombok.Builder;

/**
 * ユーザープロファイルの更新リクエスト。
 * ユーザープロファイルではログインID、会員種別の更新は不可。
 */
@Builder
record UserProfileUpdateRequest(
        @Passowrd String password,
        @UserName String userName,
        @PhoneNumber String phoneNumber,
        @Contact String contact) implements Transformable {

    UserProfileUpdateCommand toCommand() {
        return UserProfileUpdateCommand.builder()
                .password(this.password)
                .userName(this.userName)
                .phoneNumber(this.phoneNumber)
                .contact(this.contact)
                .build();
    }
}
