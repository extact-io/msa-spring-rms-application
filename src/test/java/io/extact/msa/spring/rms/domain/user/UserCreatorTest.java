package io.extact.msa.spring.rms.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.InMemoryIdentityGenerator;
import io.extact.msa.spring.rms.domain.user.UserCreator.UserModelAttributes;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class UserCreatorTest {

    private UserCreator userCreator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {

        @Bean
        UserCreator userCreator(ModelValidator validator) {
            return new UserCreator(new InMemoryIdentityGenerator(), validator);
        }

        @Bean
        @Scope("prototype")
        IdentityGenerator identityGenerator() {
            return new InMemoryIdentityGenerator();
        }
    }

    @BeforeEach
    void beforeEach(@Autowired IdentityGenerator idGenerator, @Autowired ModelValidator validator) {
        this.userCreator = new UserCreator(idGenerator, validator);
    }

    @Test
    void testCreateModel() {

        // given
        UserModelAttributes attrs = UserModelAttributes.builder()
                .loginId("loginId")
                .password("password")
                .userName("userName")
                .phoneNumber("000-0000-0000")
                .contact("contact")
                .userType(UserType.ADMIN)
                .build();

        // when
        User user = userCreator.create(attrs);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(new UserId(1));
        assertThat(user.getLoginId()).isEqualTo("loginId");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getProfile().getUserName()).isEqualTo("userName");
        assertThat(user.getProfile().getPhoneNumber()).isEqualTo("000-0000-0000");
        assertThat(user.getProfile().getContact()).isEqualTo("contact");
        assertThat(user.getUserType()).isEqualTo(UserType.ADMIN);
    }

    @Test
    void testCreateModelOnValidationError() {

        // given
        UserModelAttributes attrs = UserModelAttributes.builder()
                .loginId("loginId")
                .password("password")
                .userName("userName")
                .phoneNumber("abcd")
                .contact("contact")
                .build();

        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            userCreator.create(attrs);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf(
                        "User.userType",
                        "User.profile.phoneNumber");
    }
}
