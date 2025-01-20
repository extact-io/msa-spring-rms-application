package io.extact.msa.spring.rms.application.admin;

import static io.extact.msa.spring.rms.application.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.user.UserCreator;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;

@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class UserAdminServiceTest {

    private static final UserCreatable testCreator = new UserCreatable() {};
    private static final int WITH_SIDE_EFFECT_CASE = 99;

    @Autowired
    private UserAdminService service;

    @Configuration(proxyBeanMethods = false)
    @Import({ PersistenceConfig.class, DomainConfig.class })
    static class TestConfig {
        @Bean
        UserAdminService userAdminService(
                UserCreator modelCreator,
                DuplicateChecker<User> duplicateChecker,
                UserRepository repository) {
            return new UserAdminService(modelCreator, duplicateChecker, repository);
        }
    }

    @Test
    void testGetAll() {
        // when
        List<UserReference> users = service.getAll();

        // then
        assertThat(users).containsExactly(user1, user2, user3);
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testAdd(@Autowired UserRepository forResultAssert) {
        // given
        UserAddCommand command = UserAddCommand.builder()
                .loginId("newLogin")
                .password("newPass")
                .userType(UserType.MEMBER)
                .userName("New User")
                .phoneNumber("090-5555-5555")
                .contact("new@example.com")
                .build();

        // when
        UserReference actual = service.add(command);

        // then
        User expected = testCreator.newInstance(
                new UserId(1000),
                "newLogin",
                "newPass",
                UserType.MEMBER,
                "New User",
                "090-5555-5555",
                "new@example.com");
        assertThat(actual).isEqualTo(expected);
        // 永続化確認
        Optional<User> persisted = forResultAssert.find(expected.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(expected);
    }

    @Test
    void testAddOnDuplicate(@Autowired UserRepository forResultAssert) {
        // given
        UserAddCommand command = UserAddCommand.builder()
                .loginId(user1.getLoginId()) // 重複エラー
                .password("newPass")
                .userType(UserType.MEMBER)
                .userName("Duplicate User")
                .phoneNumber("090-6666-6666")
                .contact("duplicate@example.com")
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.add(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.DUPLICATE);
        // 永続化確認（エラー時はuser1がそのまま）
        Optional<User> persisted = forResultAssert.findDuplicationData(user1);
        assertThat(persisted).isPresent().hasValue(user1);
    }

    @Test
    void testAddOnValidationErrorOfProperty(@Autowired UserRepository forResultAssert) {
        // given
        UserAddCommand command = UserAddCommand.builder()
                .loginId("") // バリデーションエラー（未入力）
                .password("newPass")
                .userType(UserType.MEMBER)
                .userName("Invalid User")
                .phoneNumber("090-7777-7777")
                .contact("invalid@example.com")
                .build();

        // when
        RmsValidationException exception = assertThrows(RmsValidationException.class, () -> {
            service.add(command);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(exception)
                .verifyErrorItemFieldOf("User.loginId");
        // 永続化確認（エラー時は変更なし）
        List<User> users = forResultAssert.findAll();
        assertThat(users).hasSize(3);
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testUpdate(@Autowired UserRepository forResultAssert) {
        // given
        UserUpdateCommand command = UserUpdateCommand.builder()
                .id(user1.getId())
                .password("updatePass")
                .userType(UserType.ADMIN)
                .userName("Updated User")
                .phoneNumber("090-8888-8888")
                .contact("updated@example.com")
                .build();

        // when
        UserReference actual = service.update(command);

        // then
        User expected = testCreator.newInstance(
                user1.getId(),
                "login1",
                "updatedPass",
                UserType.ADMIN,
                "Updated User",
                "090-8888-8888",
                "updated@example.com");
        assertThat(actual).isEqualTo(expected);
        // 永続化確認
        Optional<User> persisted = forResultAssert.find(expected.getId());
        assertThat(persisted).isPresent().hasValue(expected);
    }

    @Test
    void testUpdateOnNotFound(@Autowired UserRepository forResultAssert) {
        // given
        UserId notFoundId = new UserId(99);
        UserUpdateCommand command = UserUpdateCommand.builder()
                .id(notFoundId)
                .password("updatedPass")
                .userType(UserType.ADMIN)
                .userName("Updated User")
                .phoneNumber("090-9999-9999")
                .contact("updated@example.com")
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.update(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
        // 永続化確認
        Optional<User> persisted = forResultAssert.find(notFoundId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    void testUpdateOnValidationErrorOfProperty(@Autowired UserRepository forResultAssert) {
        // given
        UserUpdateCommand command = UserUpdateCommand.builder()
                .id(user1.getId())
                .password("123") // バリデーションエラー
                .userType(UserType.ADMIN)
                .userName("Invalid User")
                .phoneNumber("090-0000-0000")
                .contact("invalid@example.com")
                .build();

        // when
        RmsValidationException exception = assertThrows(RmsValidationException.class, () -> {
            service.update(command);
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

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testDelete(@Autowired UserRepository forResultAssert) {
        // given
        UserId deleteId = user3.getId();

        // when
        service.delete(deleteId);

        // then
        // 永続化確認
        Optional<User> persisted = forResultAssert.find(deleteId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    void testDeleteOnNotFound(@Autowired UserRepository forResultAssert) {
        // given
        UserId notFoundId = new UserId(99);

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.delete(notFoundId);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }
}
