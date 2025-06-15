package io.extact.msa.spring.rms.webapi.universal;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.fw.feature.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.SecurityConstraintException;
import io.extact.msa.spring.rms.PersistedTestData;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.rms.webapi.universal.LoginRequest;
import io.extact.msa.spring.rms.webapi.universal.LoginUserResponse;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "file-all" })
@TestMethodOrder(OrderAnnotation.class)
class LoginControllerIntegrationTest {

    private static final LoginUserResponse user2 = LoginUserResponse.from(PersistedTestData.user2);

    @Autowired
    private LoginClient client;

    @Configuration(proxyBeanMethods = false)
    @Import(WebApiApplication.class)
    static class TestConfig {
        @Bean
        LoginClient userClient(Environment env) {
            RestClient restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();

            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(LoginClient.class);
        }
    }

    @Test
    void testLoginForGet() throws Exception {
        // given
        String loginId = "member2";
        String password = "member2";
        // when
        ResponseEntity<LoginUserResponse> actual = client.login(loginId, password);
        // then
        assertThat(actual.getHeaders().get(HttpHeaders.AUTHORIZATION)).hasSize(1);
        assertThat(actual.getBody()).isEqualTo(user2);
    }

    @Test
    void testLoginForGetOnFail() throws Exception {
        // given
        String loginId = "member99";
        String password = "member99";
        // when
        assertThatThrownBy(() -> client.login(loginId, password))
                // then
                .isInstanceOf(SecurityConstraintException.class);
    }

    @Test
    void testLoginForGetOnParameterError() throws Exception {
        // given
        String loginId = "12345678901";  // 桁数オーバー
        String password = "12345678901"; // 桁数オーバー
        // when
        assertThatThrownBy(() -> client.login(loginId, password))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(2);
                    assertThat(thrown.getDetailMessage()).contains("loginId", "password");
                });
    }

    @Test
    void testLoginForPost() throws Exception {
        // given
        String loginId = "member2";
        String password = "member2";
        LoginRequest req = new LoginRequest(loginId, password);
        // when
        ResponseEntity<LoginUserResponse> actual = client.login(req);
        // then
        assertThat(actual.getHeaders().get(HttpHeaders.AUTHORIZATION)).hasSize(1);
        assertThat(actual.getBody()).isEqualTo(user2);
    }

    @Test
    void testLoginForPostOnFail() throws Exception {
        // given
        String loginId = "member99";
        String password = "member99";
        LoginRequest req = new LoginRequest(loginId, password);
        // when
        assertThatThrownBy(() -> client.login(req))
                // then
                .isInstanceOf(SecurityConstraintException.class);
    }

    @Test
    void testLoginForPostOnParameterError() throws Exception {
        // given
        String loginId = "12345678901";  // 桁数オーバー
        String password = "12345678901"; // 桁数オーバー
        LoginRequest req = new LoginRequest(loginId, password);
        // when
        assertThatThrownBy(() -> client.login(req))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(2);
                    assertThat(thrown.getDetailMessage()).contains("loginId", "password");
                });
    }


    @HttpExchange("/login")
    public interface LoginClient {
        @GetExchange
        ResponseEntity<LoginUserResponse> login(
                @RequestParam String loginId,
                @RequestParam String password);

        @PostExchange
        ResponseEntity<LoginUserResponse> login(@RequestBody LoginRequest request);
    }
}
