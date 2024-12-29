package io.extact.msa.spring.rms.domain.user.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;

import io.extact.msa.spring.platform.fw.domain.constraint.LoginId;
import io.extact.msa.spring.platform.fw.domain.constraint.Passowrd;
import io.extact.msa.spring.platform.fw.domain.constraint.UserTypeConstraint;
import io.extact.msa.spring.platform.fw.domain.constraint.ValidationGroups.Add;
import io.extact.msa.spring.platform.fw.domain.model.DomainModel;
import io.extact.msa.spring.platform.fw.domain.type.UserType;
import io.extact.msa.spring.platform.fw.exception.RmsConstraintViolationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class User implements DomainModel {

    private @NotNull @Valid UserId id;
    private @LoginId String loginId;
    private @Passowrd String password;
    private @UserTypeConstraint UserType userType;
    private @NotNull @Valid UserProfile profile;

    private Validator validator;

    User(UserId id, String loginId, String password, UserType userType, UserProfile profile) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.userType = userType;
        this.profile = profile;
    }

    @Override
    public void configureValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void verify() {
        // 生々しいのでvalidatorをなにかで被せる
        Set<ConstraintViolation<User>> result = this.validator.validate(this, Default.class, Add.class);
        if (!result.isEmpty()) {
            throw new RmsConstraintViolationException("validation error.", new HashSet<>(result));
        }
    }

    public boolean isAdmin() {
        return this.userType == UserType.ADMIN;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void switchUserType(UserType newUserType) {
        this.userType = newUserType;
    }

    // コンストラクタを隠蔽するためのインターフェース
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
