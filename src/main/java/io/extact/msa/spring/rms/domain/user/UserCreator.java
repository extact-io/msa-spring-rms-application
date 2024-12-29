package io.extact.msa.spring.rms.domain.user;

import jakarta.validation.Validator;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.platform.fw.domain.type.UserType;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCreator {

    private final UserRepository repository;
    private final Validator validator; // TODO オレオレのModelValidatorクラスとかで被せてあげるのがよい気がする
    private final UserCreatable constructorProxy = new UserCreatable() {}; // implementsすると外部に公開してしまうためdelegateで方式にしている

    public User create(UserModelAttributes attrs) {

        UserId id = new UserId(repository.nextIdentity());
        User user = constructorProxy.newInstance(
                id,
                attrs.loginId,
                attrs.password,
                attrs.userType,
                attrs.userName,
                attrs.phoneNumber,
                attrs.contact);

        user.configureValidator(validator);
        user.verify();

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
