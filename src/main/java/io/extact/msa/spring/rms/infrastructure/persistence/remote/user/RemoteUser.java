package io.extact.msa.spring.rms.infrastructure.persistence.remote.user;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.PhysicalEntity;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;

public record RemoteUser(
        Integer id,
        String loginId,
        String password,
        String userName,
        String phoneNumber,
        String contact,
        UserType userType) implements PhysicalEntity<User>, UserCreatable {

    public static RemoteUser from(User model) {
        return new RemoteUser(
                model.getId().id(),
                model.getLoginId(),
                model.getPassword(),
                model.getProfile().getUserName(),
                model.getProfile().getPhoneNumber(),
                model.getProfile().getContact(),
                model.getUserType());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public User toModel(ModelValidator validator) {
        User user = newInstance(
                new UserId(id),
                loginId,
                password,
                userType,
                userName,
                phoneNumber,
                contact);
        user.configure(validator);
        return user;
    }
}
