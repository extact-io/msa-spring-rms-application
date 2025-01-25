package io.extact.msa.spring.rms.boundary.webapi.universal;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfig;
import io.extact.msa.spring.rms.ConstraintAnnotationAsserter;
import io.extact.msa.spring.rms.domain.user.constraint.Contact;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraint.PhoneNumber;
import io.extact.msa.spring.rms.domain.user.constraint.UserName;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class UserProfileUpdateRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidationConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        String password = "securePassword123";
        String userName = "John Doe";
        String phoneNumber = "123-456-7890";
        String contact = "john.doe@example.com";

        // when
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .password(password)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .build();

        // then
        assertThat(request.password()).isEqualTo(password);
        assertThat(request.userName()).isEqualTo(userName);
        assertThat(request.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.contact()).isEqualTo(contact);
    }

    @Test
    void testApplyAnnotationCorrectly() {
        // given
        BeanDescriptor descriptor = validator.getConstraintsForClass(UserProfileUpdateRequest.class);

        // then
        ConstraintAnnotationAsserter.asserterTo(descriptor)
                .verifyPropertyAnnotations("password", Passowrd.class)
                .verifyPropertyAnnotations("userName", UserName.class)
                .verifyPropertyAnnotations("phoneNumber", PhoneNumber.class)
                .verifyPropertyAnnotations("contact", Contact.class);
    }

    @Test
    void testValidationError() {
        // given
        UserProfileUpdateRequest invalidRequest = UserProfileUpdateRequest.builder()
                .password("") // Invalid password
                .userName("") // Invalid userName
                .phoneNumber("123") // Invalid phoneNumber
                .contact("invalid-email") // Invalid contact
                .build();

        // when
        var violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("password") &&
                        violation.getConstraintDescriptor().getAnnotation().annotationType().equals(Passowrd.class));
        assertThat(violations).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("userName") &&
                        violation.getConstraintDescriptor().getAnnotation().annotationType().equals(UserName.class));
        assertThat(violations).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("phoneNumber") &&
                        violation.getConstraintDescriptor().getAnnotation().annotationType().equals(PhoneNumber.class));
        assertThat(violations).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("contact") &&
                        violation.getConstraintDescriptor().getAnnotation().annotationType().equals(Contact.class));
    }
}
