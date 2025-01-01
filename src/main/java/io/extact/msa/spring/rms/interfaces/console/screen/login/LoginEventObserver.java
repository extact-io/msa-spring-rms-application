package io.extact.msa.spring.rms.interfaces.console.screen.login;

import io.extact.msa.spring.rms.domain.user.model.UserReference;

public interface LoginEventObserver {

    void onEvent(UserReference loginUser);

    UserReference getLoginUser();
}
