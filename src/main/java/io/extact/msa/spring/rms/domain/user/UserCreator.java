package io.extact.msa.spring.rms.domain.user;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCreator {

    private final IdentityGenerator idGenerator;
    private final ModelValidator validator;
    // implementsすると外部に公開してしまうためdelegateで方式にしている
    private final UserCreatable constructorProxy = new UserCreatable() {};

    public User create(UserModelAttributes attrs) {

        UserId id = new UserId(idGenerator.nextIdentity());
        User user = constructorProxy.newInstance(
                id,
                attrs.loginId,
                attrs.password,
                attrs.userType,
                attrs.userName,
                attrs.phoneNumber,
                attrs.contact);

        user.configureValidator(validator);
        validator.validateModel(user);

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
