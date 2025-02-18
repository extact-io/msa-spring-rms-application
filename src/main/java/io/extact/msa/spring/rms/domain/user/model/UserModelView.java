package io.extact.msa.spring.rms.domain.user.model;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;

public interface UserModelView extends EntityModelView {
    UserId getId();
    String getLoginId();
    String getPassword();
    UserType getUserType();
    UserProfile getProfile();
}