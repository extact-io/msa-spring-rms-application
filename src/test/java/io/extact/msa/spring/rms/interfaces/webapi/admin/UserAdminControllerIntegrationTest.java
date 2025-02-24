package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.SecurityConstraintException;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.interfaces.webapi.admin.UserAddRequest.UserAddRequestBuilder;
import io.extact.msa.spring.rms.interfaces.webapi.admin.UserUpdateRequest.UserUpdateRequestBuilder;
import io.extact.msa.spring.rms.testutils.PersistedTestData;
import io.extact.msa.spring.rms.testutils.TestAuthUtils;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "file-all" })
@TestMethodOrder(OrderAnnotation.class)
class UserAdminControllerIntegrationTest {

    private static final UserAdminResponse user1 = UserAdminResponse.from(PersistedTestData.user1);
    private static final UserAdminResponse user2 = UserAdminResponse.from(PersistedTestData.user2);
    private static final UserAdminResponse user3 = UserAdminResponse.from(PersistedTestData.user3);

    @Autowired
    private UserClient client;

    @Configuration(proxyBeanMethods = false)
    @Import(WebApiApplication.class)
    static class TestConfig {
        @Bean
        UserClient userClient(Environment env) {
            RestClient restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();

            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(UserClient.class);
        }
    }

    @BeforeEach
    void beforeEach(@Autowired JsonWebTokenGenerator generator) {
        TestAuthUtils.signinByJwt(generator, 1, "ADMIN");
    }

    @AfterEach
    void afterEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    @Order(1)
    void testGetAll() {
        // given
        List<UserAdminResponse> expected = List.of(user1, user2, user3);
        // when
        List<UserAdminResponse> actual = client.getAll();
        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testGetAllOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(client::getAll)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
        // when
        assertThatThrownBy(client::getAll)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(2)
    void testAdd() {
        //given
        UserAddRequest req = userAddRequestBuilder().build();
        // when
        UserAdminResponse actual = client.add(req);
        // then
        assertThat(actual).isEqualTo(new UserAdminResponse(
                4,
                req.loginId(),
                req.password(),
                req.userType(),
                req.userName(),
                req.phoneNumber(),
                req.contact()));
    }

    @Test
    void testAddOnParameterError() {

        // given
        UserAddRequest req = UserAddRequest.builder()
                .build(); // empty value
        // when
        assertThatThrownBy(() -> client.add(req))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(4);
                    assertThat(thrown.getDetailMessage()).contains("loginId", "password", "userType", "userName");
                });
    }

    @Test
    void testAddOnDuplicate() {
        // given
        UserAddRequest req = userAddRequestBuilder()
                .loginId(user3.loginId()) // override
                .build();
        // when
        assertThatThrownBy(() -> client.add(req))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.DUPLICATE);
                });
    }

    @Test
    void testAddOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        UserAddRequest req = userAddRequestBuilder().build();
        // when
        assertThatThrownBy(() -> client.add(req))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
        // when
        assertThatThrownBy(() -> client.add(req))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(3)
    void testUpdate() {
        // given
        UserUpdateRequest req = userUpdateRequestBuilder()
                .password("newPass")
                .userType(UserType.ADMIN)
                .userName("newName")
                .phoneNumber("0001231234")
                .contact("newContact")
                .build();
        // when
        UserAdminResponse actual = client.update(req);
        // then
        assertThat(actual).isEqualTo(new UserAdminResponse(
                req.id(),
                user2.loginId(),
                req.password(),
                req.userType(),
                req.userName(),
                req.phoneNumber(),
                req.contact()));
    }

    @Test
    void testUpdateOnParameterError() {
        // given
        UserUpdateRequest req = UserUpdateRequest.builder()
                .build(); // empty value
        // when
        assertThatThrownBy(() -> client.update(req))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(4);
                    assertThat(thrown.getDetailMessage()).contains("id", "password", "userType", "userName");
                });
    }

    @Test
    void testUpdateOnNotFound() {
        // given
        UserUpdateRequest request = userUpdateRequestBuilder()
                .id(999) // override
                .build();
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    void testUpdateOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        UserUpdateRequest request = userUpdateRequestBuilder().build();
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(4)
    void testDelete() {
        // given
        int deleteId = 1;
        // when
        client.delete(1);
        // then
        UserAdminResponse deleted = client.getAll().stream()
                .filter(user -> user.id() == deleteId)
                .findFirst()
                .orElse(null);
        assertThat(deleted).isNull();
    }

    @Test
    void testDeleteOnParameterError() {
        // given
        int errorId = -1;
        // when
        assertThatThrownBy(() -> client.delete(errorId))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("userId");
                });
    }

    @Test
    void testDeleteOnNotFound() {
        // given
        int notExistId = 999;
        // when
        assertThatThrownBy(() -> client.delete(notExistId))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    void testDeleteOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        int deleteId = 1;
        // when
        assertThatThrownBy(() -> client.delete(deleteId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
        // when
        assertThatThrownBy(() -> client.delete(deleteId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @HttpExchange("/admin/users")
    public interface UserClient {
        @GetExchange
        List<UserAdminResponse> getAll();

        @PostExchange
        UserAdminResponse add(@RequestBody UserAddRequest request);

        @PutExchange
        UserAdminResponse update(@RequestBody UserUpdateRequest request);

        @DeleteExchange("/{id}")
        void delete(@PathVariable("id") int userId);
    }

    private UserAddRequestBuilder userAddRequestBuilder() {
        return UserAddRequest.builder()
                .loginId("login4")
                .password("password4")
                .userType(UserType.ADMIN)
                .userName("User Three")
                .phoneNumber("111222333")
                .contact("user3@example.com");
    }

    private UserUpdateRequestBuilder userUpdateRequestBuilder() {
        return UserUpdateRequest.builder()
                .id(user2.id())
                .password(user2.loginId())
                .userType(user2.userType())
                .userName(user2.userName())
                .phoneNumber(user2.phoneNumber())
                .contact(user2.contact());
    }
}
