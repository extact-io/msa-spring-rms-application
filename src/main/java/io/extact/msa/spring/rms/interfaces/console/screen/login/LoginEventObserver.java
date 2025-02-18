package io.extact.msa.spring.rms.interfaces.console.screen.login;

import io.extact.msa.spring.rms.domain.user.model.UserModelView;

public interface LoginEventObserver {

    void onEvent(UserModelView loginUser);

    UserModelView getLoginUser();
}
