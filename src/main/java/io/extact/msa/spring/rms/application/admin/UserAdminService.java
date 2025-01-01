package io.extact.msa.spring.rms.application.admin;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.rms.application.support.ApplicationCrudSupport;
import io.extact.msa.spring.rms.domain.user.UserCreator;
import io.extact.msa.spring.rms.domain.user.UserCreator.UserModelAttributes;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserReference;

@Transactional
public class UserAdminService {

    private final UserCreator modelCreator;
    private final ApplicationCrudSupport<User> support;

    public UserAdminService(
            UserCreator modelCreator,
            DuplicateChecker<User> duplicateChecker,
            UserRepository repository) {

        this.modelCreator = modelCreator;
        this.support = new ApplicationCrudSupport<>(duplicateChecker, repository);
    }

    public List<? extends UserReference> getAll() {
        return support.getAll();
    }

    public UserReference add(UserAddCommand command) {
        return support.add(() -> this.createModel(command));
    }

    public UserReference update(UserUpdateCommand command) {
        return support.update(command.id(), user -> this.editModel(user, command));
    }

    public void delete(UserId id) {
        support.delete(id);
    }


    private User createModel(UserAddCommand command) {
        UserModelAttributes attrs = UserModelAttributes.builder()
                .loginId(command.loginId())
                .password(command.password())
                .userType(command.userType())
                .userName(command.userName())
                .phoneNumber(command.phoneNumber())
                .contact(command.contact())
                .build();
        return modelCreator.create(attrs);
    }

    private void editModel(User user, UserUpdateCommand command) {
        user.changePassword(command.password());
        user.switchUserType(command.userType());
        user.getProfile().editProfile(
                command.userName(),
                command.phoneNumber(),
                command.contact());
    }
}
