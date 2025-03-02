package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.rms.domain.user.constraint.Contact;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraint.PhoneNumber;
import io.extact.msa.spring.rms.domain.user.constraint.UserName;
import io.extact.msa.spring.rms.domain.user.constraint.UserTypeConstraint;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.test.junit5.ConstraintAnnotationAsserter;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class UserUpdateRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        Integer id = 123;
        String password = "securePassword123";
        UserType userType = UserType.ADMIN;
        String userName = "Jane Smith";
        String phoneNumber = "987-654-3210";
        String contact = "jane.smith@example.com";

        // when
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(id)
                .password(password)
                .userType(userType)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .build();

        // then
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.password()).isEqualTo(password);
        assertThat(request.userType()).isEqualTo(userType);
        assertThat(request.userName()).isEqualTo(userName);
        assertThat(request.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.contact()).isEqualTo(contact);
    }

    @Test
    void testApplyAnnotationCorrectly() {

        BeanDescriptor bd = validator.getConstraintsForClass(UserUpdateRequest.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", RmsId.class)
                .verifyPropertyAnnotations("password", Passowrd.class)
                .verifyPropertyAnnotations("userType", UserTypeConstraint.class)
                .verifyPropertyAnnotations("userName", UserName.class)
                .verifyPropertyAnnotations("phoneNumber", PhoneNumber.class)
                .verifyPropertyAnnotations("contact", Contact.class);
    }
}
