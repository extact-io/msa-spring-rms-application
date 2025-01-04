package io.extact.msa.spring.rms.application.admin;

import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import lombok.Builder;

/**
 * ユーザ管理機能に対する更新コマンドクラス。
 * ユーザ管理機能でもログインIDの変更は不可にしているためコマンドには含めていない。
 */
@Builder
public record UserUpdateCommand(
        UserId id,
        String password,
        UserType userType,
        String userName,
        String phoneNumber,
        String contact) {
}
