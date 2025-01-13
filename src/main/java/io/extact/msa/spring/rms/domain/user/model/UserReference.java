package io.extact.msa.spring.rms.domain.user.model;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelReference;

public interface UserReference extends EntityModelReference {
    UserId getId();
    String getLoginId();
    String getPassword();
    UserType getUserType();
    UserProfile getProfile();
}