package io.extact.msa.spring.rms.application.universal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.extact.msa.spring.platform.core.auth.RmsAuthentication;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;

/**
 * ユーザプロファイルサービス。
 * 実体はユーザ管理機能の一部と同じだがサービスを利用するアクターと更新範囲、
 * チェック内容がが異なるため別のサービスとして設けている。
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository repository;

    public User getOwnProfile() {
        return repository.find(getLoginUserId())
                .orElseThrow(() -> new BusinessFlowException("target does not exist for id", CauseType.NOT_FOUND));
    }

    public User updateOwnProfile(UpdateUserProfileCommand command) {

        User user = getOwnProfile();

        user.changePassword(command.password());
        user.getProfile().editProfile(command.userName(), command.phoneNumber(), command.contact());
        repository.update(user);

        return user;
    }

    private UserId getLoginUserId() {
        // TODO アプリ向けの被せものを作る
        RmsAuthentication auth = (RmsAuthentication) SecurityContextHolder.getContext().getAuthentication();
        int userId = auth.getLoginUser().getUserId();
        return new UserId(userId);
    }
}
