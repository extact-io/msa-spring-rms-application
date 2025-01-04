package io.extact.msa.spring.rms.domain.user.model;

import io.extact.msa.spring.platform.fw.domain.model.ReferenceModel;

public interface UserReference extends ReferenceModel {
    UserId getId();
    String getLoginId();
    String getPassword();
    UserType getUserType();
    UserProfileReference getProfile();
}