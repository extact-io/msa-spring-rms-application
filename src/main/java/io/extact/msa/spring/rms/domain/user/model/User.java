package io.extact.msa.spring.rms.domain.user.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.extact.msa.spring.platform.fw.domain.model.AbstractEntityModel;
import io.extact.msa.spring.rms.domain.user.constraint.LoginId;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraint.UserTypeConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString
public class User extends AbstractEntityModel implements UserModelView {

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

    User(UserId id, String loginId, String password, UserType userType, UserProfile profile) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.userType = userType;
        this.profile = profile;
    }

    // --------------------------------------- service methods

    public boolean isAdmin() {
        return this.userType == UserType.ADMIN;
    }

    public void changePassword(String newPassword) {
        applyPassword(newPassword);
    }

    public void switchUserType(UserType newUserType) {
        applyUserType(newUserType);
    }

    public void editProfile(String userName, String phoneNumber, String contact) {
        UserProfile newProfile = new UserProfile(userName, phoneNumber, contact);
        applyProfile(newProfile);
    }

    // --------------------------------------- private methods

    private void applyPassword(String newPassword) {
        User test = new User();
        test.password = newPassword;
        validator().validateField(test, "password");
        this.password = newPassword;
    }

    private void applyUserType(UserType newUserType) {
        User test = new User();
        test.userType = newUserType;
        validator().validateField(test, "userType");
        this.userType = newUserType;
    }

    private void applyProfile(UserProfile newUserProfile) {
        User test = new User();
        test.profile = newUserProfile;
        validator().validateField(test, "profile");
        this.profile = newUserProfile;
    }

    // --------------------------------------- inner interface

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
