package io.extact.msa.spring.rms.application.universal;

import lombok.Builder;

/**
 * ユーザープロファイルサービスに対する更新コマンド。
 * ユーザープロファイルサービスではログインID、会員種別の更新は不可。
 */
@Builder
public record UserProfileUpdateCommand(
        String password,
        String userName,
        String phoneNumber,
        String contact) {
}
