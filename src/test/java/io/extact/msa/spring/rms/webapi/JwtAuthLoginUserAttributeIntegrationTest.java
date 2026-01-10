package io.extact.msa.spring.rms.webapi;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;

import com.redis.testcontainers.RedisContainer;

import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.auth.configure.AuthorizeHttpRequestCustomizer;
import io.extact.msa.spring.platform.core.auth.user.AuthUserId;
import io.extact.msa.spring.platform.core.auth.user.LoginUser;
import io.extact.msa.spring.platform.core.auth.user.LoginUserAttributes;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.fw.feature.auth.RmsLoginUserAttributes;
import io.extact.msa.spring.platform.fw.infrastructure.datasource.FrameworkDataSource;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.interfaces.webapi.ApiController;
import io.extact.msa.spring.platform.fw.test.utils.TestAuthUtils;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

// TODO:
// ・Filterでエラーが出た時のハンドリングを確認する→今はerrorページに行っちゃってる
// ・Client側のシナリオテストをdocker composeを使って本物構成にしてテストを通るようにする

@TestPropertySource(properties = """
        rms.login-user-attributes.cache.enabled=true
        rms.login-user-attributes.cache.type=redis
        """)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles({ "test", "jpa-all" })
class JwtAuthLoginUserAttributeIntegrationTest {

    private static LoginUser CAPTURED_AUTH;

    @Autowired
    private RestClient client;

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import(WebApiApplication.class)
    static class TestConfig {

        @Bean
        @ServiceConnection
        @SuppressWarnings("resource")
        RedisContainer redisContainer() {
            return new RedisContainer("redis:6.2.6").withReuse(false);
        }

        @Bean
        @Primary
        PostgreSQLContainer<?> appPostgreSQLContainer() {
            return new PostgreSQLContainer<>("postgres:16-alpine");
        }

        @Bean
        @FrameworkDataSource
        PostgreSQLContainer<?> fwPostgreSQLContainer() {
            return new PostgreSQLContainer<>("postgres:16-alpine");
        }

        @Bean
        DynamicPropertyRegistrar appTargetUrlRegistrar(PostgreSQLContainer<?> postgres) { // @Primary
            return registry -> {
                registry.add("rms.datasource.applicaiton.url", postgres::getJdbcUrl);
                registry.add("rms.datasource.applicaiton.username", postgres::getUsername);
                registry.add("rms.datasource.applicaiton.password", postgres::getPassword);
            };
        }

        @Bean
        DynamicPropertyRegistrar fwTargetUrlRegistrar(@FrameworkDataSource PostgreSQLContainer<?> postgres) {
            return registry -> {
                registry.add("rms.datasource.fw.url", postgres::getJdbcUrl);
                registry.add("rms.datasource.fw.username", postgres::getUsername);
                registry.add("rms.datasource.fw.password", postgres::getPassword);
            };
        }

        @Bean
        RestClient restClient(Environment env, RestClient.Builder builder) {
            return builder
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();
        }

        @Bean
        StubController stubController() {
            return new StubController();
        }

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE) // anyRequest()をしているBeanより前に来るようにOrderを指定
        AuthorizeHttpRequestCustomizer testAuthorizeRequestCustomizer() {
            return (AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry configurer) -> configurer
                    .requestMatchers("/stub").permitAll();
        }
    }

    @AfterEach
    void afterEach() {
        CAPTURED_AUTH = null;
        TestAuthUtils.signoutQuietly();
    }


    // ----------------------------------------- test methods.

    @Test
    void testAnonymousUser() {

        // given-when
        client.get()
                .uri("/stub")
                .retrieve()
                .toBodilessEntity();

        // then
        assertThat(CAPTURED_AUTH.isAnonymousUser()).isTrue();
        assertThat(CAPTURED_AUTH.getAttributes(LoginUserAttributes.class)).isNull();
    }

    @Test
    void testAuthenticatedUser(@Autowired JsonWebTokenGenerator generator) {

        // given
        TestAuthUtils.signinByJwt(generator, 1, "ADMIN");

        // when
        client.get()
                .uri("/stub")
                .retrieve()
                .toBodilessEntity();

        // then
        RmsLoginUserAttributes expected = new RmsLoginUserAttributes(
                new AuthUserId(1),
                "ID-1の拡張属性1",
                "ID-1の拡張属性2",
                "ID-1の拡張属性3");
        assertThat(CAPTURED_AUTH.isAnonymousUser()).isFalse();
        assertThat(CAPTURED_AUTH.getAttributes(LoginUserAttributes.class)).isEqualTo(expected);
    }


    // ----------------------------------------- inner class

    @ApiController("/stub")
    static class StubController {
        @GetMapping
        void call(@AuthenticationPrincipal LoginUser loginUser) {
            CAPTURED_AUTH = loginUser;
        }
    }
}
