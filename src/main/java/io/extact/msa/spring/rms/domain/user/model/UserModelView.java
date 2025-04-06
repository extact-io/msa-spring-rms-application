package io.extact.msa.spring.rms.domain.user.model;

import java.util.Objects;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;

public interface UserModelView extends EntityModelView<UserModelView> {
    UserId getId();

    String getLoginId();

    String getPassword();

    UserType getUserType();

    UserProfile getProfile();

    @Override
    default boolean isEqual(UserModelView other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(getId(), other.getId())
                && Objects.equals(getLoginId(), other.getLoginId())
                && Objects.equals(getPassword(), other.getPassword())
                && Objects.equals(getUserType(), other.getUserType())
                && Objects.equals(getProfile(), other.getProfile());
    }
}