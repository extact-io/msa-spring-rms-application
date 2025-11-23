package io.extact.msa.spring.rms.application.universal;


import io.extact.msa.spring.platform.core.auth.context.LoginContext;
import io.extact.msa.spring.platform.fw.application.ApplicationService;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserModelView;
import lombok.RequiredArgsConstructor;

/**
 * ユーザプロファイルサービス。
 * 実体はユーザ管理機能の一部と同じだがサービスを利用するアクターと更新範囲、
 * チェック内容がが異なるため別のサービスとして設けている。
 */
@RequiredArgsConstructor
@ApplicationService
public class UserProfileService {

    private final LoginContext loginContext;
    private final UserRepository repository;

    public UserModelView getOwnProfile() {
        return getInternalOwnProfile();
    }

    public UserModelView updateOwnProfile(UserProfileUpdateCommand command) {

        User user = getInternalOwnProfile();
        user.changePassword(command.password());
        user.editProfile(
                command.userName(),
                command.phoneNumber(),
                command.contact());

        repository.update(user);

        return user;
    }

    private User getInternalOwnProfile() {
        int loginUserId = loginContext.getLoginUser().getUserId().value();
        return repository
                .find(new UserId(loginUserId))
                .orElseThrow(() -> new BusinessFlowException(
                        "target does not exist for id", CauseType.NOT_FOUND));
    }
}
