package io.extact.msa.spring.rms.application.universal;

import static io.extact.msa.spring.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.core.auth.context.DefaultLoginContext;
import io.extact.msa.spring.platform.core.auth.context.LoginContext;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;
import io.extact.msa.spring.rms.test.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.test.TestAuthUtils;

@DataJpaTest
@ActiveProfiles({ "test", "jpa-all" })
@TestMethodOrder(OrderAnnotation.class)
class UserProfileServiceTest {

    private static final UserCreatable testCreator = new UserCreatable() {};
    private static final int WITH_SIDE_EFFECT_CASE = 99;

    @Autowired
    private UserProfileService service;

    @Configuration(proxyBeanMethods = false)
    @Import({
            PersistenceConfig.class,
            DomainConfig.class })
    static class TestConfig {

        @Bean
        LoginContext loginContext() {
            return new DefaultLoginContext();
        }

        @Bean
        UserProfileService userProfileService(LoginContext loginContext, UserRepository repository) {
            return new UserProfileService(loginContext, repository);
        }
    }

    @BeforeEach
    void beforeEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void updateOwnProfile(@Autowired UserRepository forResultAssert) {
        // given
        UserProfileUpdateCommand command = UserProfileUpdateCommand.builder()
                .password("updatePass")
                .userName("updateName")
                .phoneNumber("090-5555-5555")
                .contact("update@example.com")
                .build();
        TestAuthUtils.signinByHeader(1, "MEMBER");

        // when
        UserReference actual = service.updateOwnProfile(command);

        // then
        User expected = testCreator.newInstance(
                user1.getId(),
                user1.getLoginId(),
                command.password(),
                user1.getUserType(),
                command.userName(),
                command.phoneNumber(),
                command.contact());
        assertThat(actual).isEqualTo(expected);
        // 永続化確認
        Optional<User> persisted = forResultAssert.find(expected.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(expected);
    }

    @Test
    void updateOwnProfileOnNotFound(@Autowired UserRepository forResultAssert) {
        // given
        UserProfileUpdateCommand command = UserProfileUpdateCommand.builder()
                .password("updatePass")
                .userName("updateName")
                .phoneNumber("090-5555-5555")
                .contact("update@example.com")
                .build();
        TestAuthUtils.signinByHeader(99, "MEMBER");

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.updateOwnProfile(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }

    @Test
    void updateOwnProfileOnValidationErrorOfProperty(@Autowired UserRepository forResultAssert) {
        // given
        UserProfileUpdateCommand command = UserProfileUpdateCommand.builder()
                .password("charSizeOver") // サイズえらー
                .userName("updateName")
                .phoneNumber("090-5555-5555")
                .contact("update@example.com")
                .build();
        TestAuthUtils.signinByHeader(1, "MEMBER");

        // when
        RmsValidationException exception = assertThrows(RmsValidationException.class, () -> {
            service.updateOwnProfile(command);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(exception)
            .verifyErrorItemFieldOf("User.password");
        // 永続化確認
        Optional<User> persisted = forResultAssert.find(user1.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(user1);
    }
}
