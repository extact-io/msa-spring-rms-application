package io.extact.msa.spring.rms.interfaces.webapi.universal;

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
import io.extact.msa.spring.rms.domain.user.constraint.LoginId;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import io.extact.msa.spring.rms.interfaces.webapi.universal.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class LoginRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidationConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        String loginId = "testUser";
        String password = "securePassword123";

        // when
        LoginRequest request = LoginRequest.builder()
                .loginId(loginId)
                .password(password)
                .build();

        // then
        assertThat(request.loginId()).isEqualTo(loginId);
        assertThat(request.password()).isEqualTo(password);
    }

    @Test
    void testApplyAnnotationCorrectly() {
        // given
        BeanDescriptor descriptor = validator.getConstraintsForClass(LoginRequest.class);

        // then
        ConstraintAnnotationAsserter.asserterTo(descriptor)
                .verifyPropertyAnnotations("loginId", LoginId.class)
                .verifyPropertyAnnotations("password", Passowrd.class);
    }

    @Test
    void testValidationError() {
        // given
        LoginRequest invalidRequest = LoginRequest.builder()
                .loginId("") // Invalid loginId
                .password("short") // Invalid password (e.g., too short)
                .build();

        // when
        var violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("loginId") &&
                        violation.getConstraintDescriptor().getAnnotation().annotationType().equals(LoginId.class));
        assertThat(violations).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("password") &&
                        violation.getConstraintDescriptor().getAnnotation().annotationType().equals(Passowrd.class));
    }
}
