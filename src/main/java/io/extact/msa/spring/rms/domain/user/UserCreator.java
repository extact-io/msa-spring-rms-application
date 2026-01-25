package io.extact.msa.spring.rms.domain.user;

import io.extact.msa.spring.platform.fw.domain.model.ModelCreator;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.repository.IdProvider;
import io.extact.msa.spring.rms.domain.user.UserCreator.UserModelAttributes;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserCreator implements ModelCreator<User, UserModelAttributes> {

    private final IdProvider<UserId> idProvider;
    private final ModelValidator validator;
    private final UserCreatable constructorProxy = new UserCreatable() {};

    public User create(UserModelAttributes attrs) {

        UserId id = idProvider.nextIdentity();
        User user = constructorProxy.newInstance(
                id,
                attrs.loginId,
                attrs.password,
                attrs.userType,
                attrs.userName,
                attrs.phoneNumber,
                attrs.contact);

        user.configure(validator);
        user.verify();
        user.register();

        return user;
    }

    @Builder
    public static class UserModelAttributes {

        private String loginId;
        private String password;
        private UserType userType;
        private String userName;
        private String phoneNumber;
        private String contact;
    }
}
