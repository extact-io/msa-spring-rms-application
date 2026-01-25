package io.extact.msa.spring.rms.application.admin;

import java.util.ArrayList;
import java.util.List;

import io.extact.msa.spring.platform.core.transaction.ReadOnly;
import io.extact.msa.spring.platform.fw.application.ApplicationCrudSupport;
import io.extact.msa.spring.platform.fw.application.ApplicationService;
import io.extact.msa.spring.platform.fw.application.event.ApplicationServiceEventPublisher;
import io.extact.msa.spring.platform.fw.domain.service.DomainEventPublisher;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.rms.application.admin.event.UserWillBeDeletedEvent;
import io.extact.msa.spring.rms.domain.user.UserCreator;
import io.extact.msa.spring.rms.domain.user.UserCreator.UserModelAttributes;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserModelView;

@ApplicationService
public class UserAdminService {

    private final UserCreator modelCreator;
    private final ApplicationCrudSupport<User> support;
    private final ApplicationServiceEventPublisher applicationEventPublisher;

    public UserAdminService(
            UserCreator modelCreator,
            DuplicateChecker<User> duplicateChecker,
            UserRepository repository,
            ApplicationServiceEventPublisher eventPublisher,
            DomainEventPublisher domainEventPublisher) {

        this.modelCreator = modelCreator;
        this.support = new ApplicationCrudSupport<>(duplicateChecker, repository, domainEventPublisher);
        this.applicationEventPublisher = eventPublisher;
    }

    @ReadOnly
    public List<UserModelView> getAll() {
        return new ArrayList<>(support.getAll());
    }

    public UserModelView add(UserAddCommand command) {
        return support.add(() -> this.createModel(command));
    }

    public UserModelView update(UserUpdateCommand command) {
        // loginIdは更新不可なので重複エラーは発生しない
        return support.update(command.id(), user -> this.editModel(user, command));
    }

    public void delete(UserId id) {
        applicationEventPublisher.publish(new UserWillBeDeletedEvent(id));
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
        user.editProfile(
                command.userName(),
                command.phoneNumber(),
                command.contact());
    }
}
