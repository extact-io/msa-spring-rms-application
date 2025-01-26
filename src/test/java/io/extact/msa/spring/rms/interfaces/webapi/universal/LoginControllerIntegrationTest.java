package io.extact.msa.spring.rms.interfaces.webapi.universal;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.extact.msa.spring.PersistedTestData;
import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.jwt.encode.JwtEncodeConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "file-all", "test" })
@TestMethodOrder(OrderAnnotation.class)
class LoginControllerIntegrationTest {

    private static final LoginUserResponse user2 = LoginUserResponse.from(PersistedTestData.user2);

    @Autowired
    private LoginClient client;

    @Configuration(proxyBeanMethods = false)
    @Import({
        WebApiApplication.class,
        JwtEncodeConfig.class
    })
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
        LoginUserResponse actual = client.login(loginId, password);
        // then
        // TODO jwtの発行も確認
        assertThat(actual).isEqualTo(user2);
    }

    @Test
    void testLoginForGetOnFail() throws Exception {
        // given
        String loginId = "member99";
        String password = "member99";
        // when
        assertThatThrownBy(() -> client.login(loginId, password))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
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
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(2);
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
        LoginUserResponse actual = client.login(req);
        // then
        // TODO jwtの発行も確認
        assertThat(actual).isEqualTo(user2);
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
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
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
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(2);
                    assertThat(thrown.getDetailMessage()).contains("loginId", "password");
                });
    }


    @HttpExchange("/login")
    public interface LoginClient {
        @GetExchange
        LoginUserResponse login(@RequestParam("loginId") String loginId, @RequestParam("password") String password);

        @PostExchange
        LoginUserResponse login(@RequestBody LoginRequest request);
    }
}
