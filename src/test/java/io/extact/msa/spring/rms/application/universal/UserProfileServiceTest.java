package io.extact.msa.spring.rms.application.universal;

import static io.extact.msa.spring.rms.application.PersistedTestData.*;
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

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.test.stub.auth.TestAuthUtils;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;

@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class UserProfileServiceTest {

    private static final UserCreatable testCreator = new UserCreatable() {};
    private static final int WITH_SIDE_EFFECT_CASE = 99;

    @Autowired
    private UserProfileService service;

    @Configuration(proxyBeanMethods = false)
    @Import({ PersistenceConfig.class })
    static class TestConfig {
        @Bean
        UserProfileService userProfileService(UserRepository repository) {
            return new UserProfileService(repository);
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
        TestAuthUtils.signin(1, "MEMBER");

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
        TestAuthUtils.signin(99, "MEMBER");

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
        TestAuthUtils.signin(1, "MEMBER");

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
