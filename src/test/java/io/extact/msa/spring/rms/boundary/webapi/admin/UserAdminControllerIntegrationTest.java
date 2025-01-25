package io.extact.msa.spring.rms.boundary.webapi.admin;

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
import io.extact.msa.spring.platform.core.jwt.encode.JwtEncodeConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.SecurityConstraintException;
import io.extact.msa.spring.platform.test.stub.auth.TestAuthUtils;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "file-all", "test" })
@TestMethodOrder(OrderAnnotation.class)
public class UserAdminControllerIntegrationTest {

    private static final UserAdminResponse user1 = new UserAdminResponse(1, "login1", "password1", UserType.ADMIN, "User One", "123456789", "user1@example.com");
    private static final UserAdminResponse user2 = new UserAdminResponse(2, "login2", "password2", UserType.MEMBER, "User Two", "987654321", "user2@example.com");

    @Autowired
    private UserClient client;

    @Configuration(proxyBeanMethods = false)
    @Import({
        WebApiApplication.class,
        JwtEncodeConfig.class
    })
    static class TestConfig {
        @Bean
        UserClient userClient(Environment env) {
            var restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();

            var factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
            return factory.createClient(UserClient.class);
        }
    }

    @BeforeEach
    void beforeEach(@Autowired JsonWebTokenGenerator generator) {
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
    }

    @AfterEach
    void afterEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    @Order(1)
    void testGetAll() {
        List<UserAdminResponse> expected = List.of(user1, user2);
        List<UserAdminResponse> actual = client.getAll();
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testGetAllOnAuthenticationError() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(client::getAll)
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown ->
                        assertThat(thrown).hasMessageContaining("認証エラー"));
    }

    @Test
    @Order(2)
    void testAdd() {
        UserAddRequest request = UserAddRequest.builder()
                .loginId("login3")
                .password("password3")
                .userType(UserType.ADMIN)
                .userName("User Three")
                .phoneNumber("111222333")
                .contact("user3@example.com")
                .build();

        UserAdminResponse actual = client.add(request);
        assertThat(actual).isEqualTo(new UserAdminResponse(3, "login3", "password3", UserType.ADMIN, "User Three", "111222333", "user3@example.com"));
    }

    @Test
    void testAddOnParameterError() {
        UserAddRequest request = UserAddRequest.builder().build();

        assertThatThrownBy(() -> client.add(request))
                .isInstanceOfSatisfying(RmsValidationException.class, thrown ->
                        assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1));
    }

    @Test
    void testAddOnDuplicate() {
        UserAddRequest request = UserAddRequest.builder()
                .loginId("login1")
                .password("password1")
                .userType(UserType.ADMIN)
                .userName("User One")
                .phoneNumber("123456789")
                .contact("user1@example.com")
                .build();

        assertThatThrownBy(() -> client.add(request))
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown ->
                        assertThat(thrown.getCauseType()).isEqualTo(CauseType.DUPLICATE));
    }

    @Test
    void testAddOnAuthenticationError() {
        SecurityContextHolder.clearContext();
        UserAddRequest request = UserAddRequest.builder()
                .loginId("login3")
                .password("password3")
                .userType(UserType.ADMIN)
                .userName("User Three")
                .phoneNumber("111222333")
                .contact("user3@example.com")
                .build();

        assertThatThrownBy(() -> client.add(request))
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown ->
                        assertThat(thrown).hasMessageContaining("認証エラー"));
    }

    @Test
    @Order(3)
    void testUpdate() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(2)
                .password("newPassword")
                .userType(UserType.MEMBER)
                .userName("Updated User")
                .phoneNumber("987654321")
                .contact("updated@example.com")
                .build();

        UserAdminResponse actual = client.update(request);
        assertThat(actual).isEqualTo(new UserAdminResponse(2, "login2", "newPassword", UserType.MEMBER, "Updated User", "987654321", "updated@example.com"));
    }

    @Test
    void testUpdateOnParameterError() {
        UserUpdateRequest request = UserUpdateRequest.builder().build();

        assertThatThrownBy(() -> client.update(request))
                .isInstanceOfSatisfying(RmsValidationException.class, thrown ->
                        assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1));
    }

    @Test
    void testUpdateOnNotFound() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(999)
                .password("password")
                .build();

        assertThatThrownBy(() -> client.update(request))
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown ->
                        assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND));
    }

    @Test
    void testUpdateOnDuplicate() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(2)
                .password("duplicatePassword")
                .build();

        assertThatThrownBy(() -> client.update(request))
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown ->
                        assertThat(thrown.getCauseType()).isEqualTo(CauseType.DUPLICATE));
    }

    @Test
    void testUpdateOnAuthenticationError() {
        SecurityContextHolder.clearContext();
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(2)
                .password("password")
                .build();

        assertThatThrownBy(() -> client.update(request))
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown ->
                        assertThat(thrown).hasMessageContaining("認証エラー"));
    }

    @Test
    @Order(4)
    void testDelete() {
        client.delete(1);

        List<UserAdminResponse> remaining = client.getAll();
        assertThat(remaining).doesNotContain(user1);
    }

    @Test
    void testDeleteOnNotFound() {
        assertThatThrownBy(() -> client.delete(999))
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown ->
                        assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND));
    }

    @Test
    void testDeleteOnAuthenticationError() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> client.delete(1))
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown ->
                        assertThat(thrown).hasMessageContaining("認証エラー"));
    }

    @HttpExchange("/users")
    public interface UserClient {
        @GetExchange
        List<UserAdminResponse> getAll();

        @PostExchange
        UserAdminResponse add(UserAddRequest request);

        @PutExchange
        UserAdminResponse update(UserUpdateRequest request);

        @DeleteExchange("/{id}")
        void delete(int userId);
    }
}
