package io.extact.msa.spring.rms.application.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.rms.application.support.ApplicationCrudSupport;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.UserCreator;
import io.extact.msa.spring.rms.domain.user.UserCreator.UserModelAttributes;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;

@Service
public class UserManagementService {

    private final UserCreator modelCreator;
    private final UserRepository repository;
    private final ApplicationCrudSupport<User> support;

    public UserManagementService(
            UserCreator modelCreator,
            DuplicateChecker<User> duplicateChecker,
            UserRepository repository) {

        this.modelCreator = modelCreator;
        this.repository = repository;
        this.support = new ApplicationCrudSupport<>(duplicateChecker, repository);
    }

    public List<User> getAll() {
        return support.getAll();
    }

    public Optional<User> getById(UserId id) {
        return repository.find(id);
    }

    public User add(AddUserCommand command) {
        return support.add(() -> this.createModel(command));
    }

    public User update(UpdateUserCommand command) {
        return support.update(command.id(), user -> this.editModel(user, command));
    }

    public void delete(ItemId id) {
        support.delete(id);
    }


    private User createModel(AddUserCommand command) {
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

    private void editModel(User user, UpdateUserCommand command) {
        user.changePassword(command.password());
        user.switchUserType(command.userType());
        user.getProfile().editProfile(
                command.userName(),
                command.phoneNumber(),
                command.contact());
    }
}
