package io.extact.msa.spring.rms.interfaces.webapi.universal;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
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
import io.extact.msa.spring.rms.interfaces.webapi.universal.UserProfileUpdateRequest.UserProfileUpdateRequestBuilder;
import io.extact.msa.spring.rms.test.PersistedTestData;
import io.extact.msa.spring.rms.test.TestAuthUtils;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "file-all" })
@TestMethodOrder(OrderAnnotation.class)
class UserProfileControllerIntegrationTest {

    private static final UserProfileResponse user2Member = UserProfileResponse.from(PersistedTestData.user2);
    private static final UserProfileResponse user3Admin = UserProfileResponse.from(PersistedTestData.user3);

    @Autowired
    private ProfileClient client;

    @Configuration(proxyBeanMethods = false)
    @Import(WebApiApplication.class)
    static class TestConfig {
        @Bean
        ProfileClient userClient(Environment env) {
            RestClient restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();

            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(ProfileClient.class);
        }
    }

    @Test
    @Order(1)
    void testGetOwnProfile(@Autowired JsonWebTokenGenerator generator) {
        // given for member
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 2, "MEMBER");
        // when
        UserProfileResponse actual = client.getOwnProfile();
        // then
        assertThat(actual).isEqualTo(user2Member);

        // given for amdmin
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        actual = client.getOwnProfile();
        // then
        assertThat(actual).isEqualTo(user3Admin);
    }

    @Test
    void testGetOwnProfileOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(client::getOwnProfile)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(2)
    void testUpdateOwnProfile(@Autowired JsonWebTokenGenerator generator) {
        // given
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 2, "MEMBER");

        UserProfileUpdateRequest req = userProfileUpdateRequestBuilder()
                .password("newPass")
                .userName("newName")
                .phoneNumber("0001231234")
                .contact("newContact")
                .build();
        // when
        UserProfileResponse actual = client.updateOwnProfile(req);
        // then
        assertThat(actual).isEqualTo(new UserProfileResponse(
                user2Member.id(),
                user2Member.loginId(),
                req.password(),
                user2Member.userType(),
                req.userName(),
                req.phoneNumber(),
                req.contact()));
    }

    @Test
    void testUpdateOwnProfileOnParameterError(@Autowired JsonWebTokenGenerator generator) {
        // given
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 2, "MEMBER");
        UserProfileUpdateRequest req = UserProfileUpdateRequest.builder()
                .build(); // empty value
        // when
        assertThatThrownBy(() -> client.updateOwnProfile(req))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(2);
                    assertThat(thrown.getDetailMessage()).contains("password", "userName");
                });
    }

    @Test
    void testUpdateOwnProfileOnNotFound(@Autowired JsonWebTokenGenerator generator) {
        // given
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 999, "MEMBER");
        UserProfileUpdateRequest req = userProfileUpdateRequestBuilder().build();
        // when
        assertThatThrownBy(() -> client.updateOwnProfile(req))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    void testUpdateOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        UserProfileUpdateRequest req = userProfileUpdateRequestBuilder().build();
        // when
        assertThatThrownBy(() -> client.updateOwnProfile(req))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @HttpExchange("/profiles")
    public interface ProfileClient {
        @GetExchange("/own")
        UserProfileResponse getOwnProfile();

        @PutExchange("/own")
        UserProfileResponse updateOwnProfile(@RequestBody UserProfileUpdateRequest request);
    }

    private UserProfileUpdateRequestBuilder userProfileUpdateRequestBuilder() {
        return UserProfileUpdateRequest.builder()
                .password(user2Member.loginId())
                .userName(user2Member.userName())
                .phoneNumber(user2Member.phoneNumber())
                .contact(user2Member.contact());
    }
}
