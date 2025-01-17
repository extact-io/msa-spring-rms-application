package io.extact.msa.spring.rms.domain.user.model;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.framework.model.DefaultModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.ConstraintAnnotationAsserter;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.user.constraints.Contact;
import io.extact.msa.spring.rms.domain.user.constraints.LoginId;
import io.extact.msa.spring.rms.domain.user.constraints.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraints.PhoneNumber;
import io.extact.msa.spring.rms.domain.user.constraints.UserName;
import io.extact.msa.spring.rms.domain.user.constraints.UserTypeConstraint;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class UserTest {

    @Autowired
    private ModelValidator modelValidator;
    @Autowired
    private Validator beanValidator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testApplyAnnotationCorrectly() {

        BeanDescriptor bd = beanValidator.getConstraintsForClass(UserId.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", RmsId.class);

        bd = beanValidator.getConstraintsForClass(User.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", NotNull.class)
                .verifyPropertyAnnotations("loginId", LoginId.class)
                .verifyPropertyAnnotations("password", Passowrd.class)
                .verifyPropertyAnnotations("userType", UserTypeConstraint.class)
                .verifyPropertyAnnotations("profile", NotNull.class);

        bd = beanValidator.getConstraintsForClass(UserProfile.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("userName", UserName.class)
                .verifyPropertyAnnotations("phoneNumber", PhoneNumber.class)
                .verifyPropertyAnnotations("contact", Contact.class);
    }

    @Test
    void testChangePasswordOK() {
        // geven
        User user = newUser();

        UserId oldId = user.getId();
        String oldLoginId = user.getLoginId();
        UserType oldUserType = user.getUserType();
        UserProfile oldProfile = user.getProfile();

        String newPassword = "newPasswor";

        // when
        assertThatCode(() -> {
            user.changePassword(newPassword);
        })
        // then
        .doesNotThrowAnyException();

        // 変更箇所が反映されていること
        assertThat(user.getPassword()).isEqualTo(newPassword);
        // 関係ない個所に副作用が発生していないこと
        assertThat(user.getId()).isEqualTo(oldId);
        assertThat(user.getLoginId()).isEqualTo(oldLoginId);
        assertThat(user.getUserType()).isEqualTo(oldUserType);
        assertThat(user.getProfile()).isEqualTo(oldProfile);
    }

    @Test
    void testIsAdmin() {
        // geven
        User user = newUser();
        // when
        boolean isAdmin = user.isAdmin();
        // then
        assertThat(isAdmin).isTrue();

        // geven
        user.switchUserType(UserType.MEMBER);
        // when
        isAdmin = user.isAdmin();
        // then
        assertThat(isAdmin).isFalse();
    }

    @Test
    void testChangePasswordNG() {
        // geven
        User user = newUser();

        String oldPassword = user.getPassword();
        String newPassword = "newPassword";

        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            user.changePassword(newPassword);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf("User.password");

        // モデルが更新されてないこと
        assertThat(user.getPassword()).isEqualTo(oldPassword);
    }

    @Test
    void testSwitchUserTypeOK() {
        // geven
        User user = newUser();

        UserId oldId = user.getId();
        String oldLoginId = user.getLoginId();
        String oldPassword = user.getPassword();
        UserProfile oldProfile = user.getProfile();

        UserType newUserType = UserType.MEMBER;

        // when
        assertThatCode(() -> {
            user.switchUserType(newUserType);
        })
        // then
        .doesNotThrowAnyException();

        // 変更箇所が反映されていること
        assertThat(user.getUserType()).isEqualTo(newUserType);
        // 関係ない個所に副作用が発生していないこと
        assertThat(user.getId()).isEqualTo(oldId);
        assertThat(user.getLoginId()).isEqualTo(oldLoginId);
        assertThat(user.getPassword()).isEqualTo(oldPassword);
        assertThat(user.getProfile()).isEqualTo(oldProfile);
    }

    @Test
    void testSwitchUserTypeNG() {
        // geven
        User user = newUser();

        UserType oldUserType = user.getUserType();
        UserType newUserType = null;

        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            user.switchUserType(newUserType);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf("User.userType");

        // モデルが更新されてないこと
        assertThat(user.getUserType()).isEqualTo(oldUserType);
    }

    @Test
    void testEditProfileOK() {
        // geven
        User user = newUser();

        UserId oldId = user.getId();
        String oldLoginId = user.getLoginId();
        String oldPassword = user.getPassword();
        UserType oldUserType = user.getUserType();

        UserProfile newProfile = new UserProfile(
                "newUserName",
                "090-0000-0000",
                "newContact");

        // when
        assertThatCode(() -> {
            user.editProfile(
                    newProfile.getUserName(),
                    newProfile.getPhoneNumber(),
                    newProfile.getContact());
        })
        // then
        .doesNotThrowAnyException();

        // 変更箇所が反映されていること
        assertThat(user.getProfile()).isEqualTo(newProfile);
        // 関係ない個所に副作用が発生していないこと
        assertThat(user.getId()).isEqualTo(oldId);
        assertThat(user.getLoginId()).isEqualTo(oldLoginId);
        assertThat(user.getPassword()).isEqualTo(oldPassword);
        assertThat(user.getUserType()).isEqualTo(oldUserType);
    }

    @Test
    void testEditProfileNG() {
        // geven
        User user = newUser();

        UserProfile oldProfile = user.getProfile();
        UserProfile newProfile = new UserProfile(
                "", // 未入力
                "090-1234-aaaa", // 使用不可文字
                "12345678901234567890123456789012345678901"); // 文字数オーバー

        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            user.editProfile(
                    newProfile.getUserName(),
                    newProfile.getPhoneNumber(),
                    newProfile.getContact());
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf(
                        "User.profile.userName",
                        "User.profile.phoneNumber",
                        "User.profile.contact");

        // モデルが更新されてないこと
        assertThat(user.getProfile()).isEqualTo(oldProfile);
    }

    private User newUser() {
        User user = new User(
                new UserId(1),
                "loginId",
                "password",
                UserType.ADMIN,
                new UserProfile(
                        "userName",
                        "090-0000-0000",
                        "contact"));
        user.configureSupport(new DefaultModelPropertySupportFactory(modelValidator));
        return user;
    }
}
