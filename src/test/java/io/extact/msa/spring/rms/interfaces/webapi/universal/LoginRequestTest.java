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
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.domain.user.constraint.LoginId;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import io.extact.msa.spring.rms.testutils.ConstraintAnnotationAsserter;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class LoginRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
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
}
