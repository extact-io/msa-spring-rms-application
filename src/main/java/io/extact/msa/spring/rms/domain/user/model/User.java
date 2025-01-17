package io.extact.msa.spring.rms.domain.user.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.extact.msa.spring.platform.fw.domain.model.EntityModel;
import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupport;
import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupportFactory;
import io.extact.msa.spring.rms.domain.user.constraints.LoginId;
import io.extact.msa.spring.rms.domain.user.constraints.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraints.UserTypeConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
public class User implements EntityModel, UserReference {

    @Getter
    @NotNull
    @Valid
    private UserId id;
    @Getter
    @LoginId
    private String loginId;
    @Getter
    @Passowrd
    private String password;
    @Getter
    @UserTypeConstraint
    private UserType userType;
    @Getter
    @NotNull
    @Valid
    private UserProfile profile;

    private ModelPropertySupport modelSupport;

    User(UserId id, String loginId, String password, UserType userType, UserProfile profile) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.userType = userType;
        this.profile = profile;
    }

    public boolean isAdmin() {
        return this.userType == UserType.ADMIN;
    }

    public void changePassword(String newPassword) {
        modelSupport.setPropertyWithValidation("password", newPassword);
    }

    public void switchUserType(UserType newUserType) {
        modelSupport.setPropertyWithValidation("userType", newUserType);
    }

    public void editProfile(String userName, String phoneNumber, String contact) {
        UserProfile newProfile = new UserProfile(userName, phoneNumber, contact);
        modelSupport.setPropertyWithValidation("profile", newProfile);
    }

    @Override
    public void configureSupport(ModelPropertySupportFactory factory) {
        this.modelSupport = factory.create(User::new, this);
    }

    public interface UserCreatable {
        default User newInstance(
                UserId id,
                String loginId,
                String password,
                UserType userType,
                String userName,
                String phoneNumber,
                String contact) {

            return new User(
                    id,
                    loginId,
                    password,
                    userType,
                    new UserProfile(
                            userName,
                            phoneNumber,
                            contact));
        }
    }
}
