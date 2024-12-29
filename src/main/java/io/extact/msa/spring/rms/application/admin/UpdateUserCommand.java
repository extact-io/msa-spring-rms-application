package io.extact.msa.spring.rms.application.admin;

import io.extact.msa.spring.platform.fw.domain.type.UserType;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;

/**
 * ユーザ管理機能に対する更新コマンドクラス。
 * ユーザ管理機能でもログインIDの変更は不可にしているためコマンドには含めていない。
 */
@Builder
public record UpdateUserCommand(
        UserId id,
        String password,
        UserType userType,
        String userName,
        String phoneNumber,
        String contact) {
}
