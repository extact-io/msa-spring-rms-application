package io.extact.msa.spring.rms.application.universal;

/**
 * ユーザープロファイルサービスに対する更新コマンド。
 * ユーザープロファイルサービスではログインID、会員種別の更新は不可。
 */
public record UpdateUserProfileCommand(
        String password,
        String userName,
        String phoneNumber,
        String contact) {
}
