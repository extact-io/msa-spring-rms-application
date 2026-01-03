package io.extact.msa.spring.rms.webapi.universal;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.client.RestClient;

import io.extact.msa.spring.platform.core.auth.RmsAuthentication;
import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.auth.user.LoginUser;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.interfaces.webapi.ApiController;
import io.extact.msa.spring.platform.fw.test.utils.TestAuthUtils;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
//@EnableAutoConfigurationWithoutJpa
@EnableWebSecurity(debug = true)
@ActiveProfiles({ "test", "file-all" })
class LoginUserAttributeIntegrationTest {


    @Autowired
    private RestClient client;
    private static RmsAuthentication CAPTURED_AUTH;

    @ApiController("/login")
    static class StubController {


        // MEMO:
        // @AuthenticationPrincipalの場合はAnonymousでもインジェクションされるが、
        // Authenticationインタフェースの場合、Anonymousはnullになる
        // なので、RestControllerで取得する場合は@AuthenticationPrincipalで取得するのが正解と思われる

        // TODO:
        // Customizerを複数取れるようにして、Login以外のパスを追加できるようにする

        @PutMapping
        void call(@AuthenticationPrincipal LoginUser loginUser) {
            Authentication auth2 = SecurityContextHolder.getContext().getAuthentication();
            CAPTURED_AUTH = (RmsAuthentication) auth2;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Import(WebApiApplication.class)
    static class TestConfig {

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
    }

    @BeforeEach
    void beforeEach() {
    }

    @AfterEach
    void afterEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    void testAnonymousAuth(@Autowired JsonWebTokenGenerator generator) {
        //TestAuthUtils.signinByJwt(generator, 1, "ADMIN");
        client.put()
                .uri("/login")
                .retrieve()
                .toBodilessEntity();
        System.out.println(CAPTURED_AUTH);
    }
}
