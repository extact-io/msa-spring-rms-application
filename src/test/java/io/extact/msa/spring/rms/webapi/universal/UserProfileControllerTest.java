package io.extact.msa.spring.rms.webapi.universal;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.application.universal.UserProfileService;
import io.extact.msa.spring.rms.application.universal.UserProfileUpdateCommand;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.webapi.WebApiConfig.WebApiContextConfigs;

@WebMvcTest
@ActiveProfiles("test")
class UserProfileControllerTest {

    private static final UserCreatable testCreator = new UserCreatable() {
    };

    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private UserProfileService profileService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            WebApiContextConfigs.class
    })
    static class TestConfig {
        @Bean
        UserProfileController userProfileController(UserProfileService service) {
            return new UserProfileController(service);
        }
    }

    @Test
    @WithMockUser
    void testGetOwnProfile() throws Exception {

        // given
        when(profileService.getOwnProfile()).thenReturn(user1);

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/profiles/own")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(user1.getId().id()))
                .hasPathSatisfying("$.loginId", p -> p.assertThat().isEqualTo(user1.getLoginId()))
                .hasPathSatisfying("$.password", p -> p.assertThat().isEqualTo(user1.getPassword()))
                .hasPathSatisfying("$.userType", p -> p.assertThat().isEqualTo(user1.getUserType().name()))
                .hasPathSatisfying("$.userName", p -> p.assertThat().isEqualTo(user1.getProfile().getUserName()))
                .hasPathSatisfying("$.phoneNumber", p -> p.assertThat().isEqualTo(user1.getProfile().getPhoneNumber()))
                .hasPathSatisfying("$.contact", p -> p.assertThat().isEqualTo(user1.getProfile().getContact()));
    }

    @Test
    @WithMockUser
    void testGetOwnProfileOnNotFound() throws Exception {

        // given
        when(profileService.getOwnProfile())
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/profiles/own")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyText()
                .contains("NOT_FOUND");
    }

    @Test
    void testGetOwnProfileOnAuthenticationError() throws Exception {
        // given
        // @WithMockUserなし

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/profiles/own")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(profileService, never()).getOwnProfile();
    }

    @Test
    @WithMockUser
    void testUpdateOwnProfile() throws Exception {

        // given
        UserProfileUpdateRequest req = createUserProfileUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        UserProfileUpdateCommand shouldBePassed = req.toCommand();
        when(profileService.updateOwnProfile(shouldBePassed))
                .thenReturn(testCreator.newInstance(
                        user1.getId(),
                        user1.getLoginId(),
                        req.password(),
                        user1.getUserType(),
                        req.userName(),
                        req.phoneNumber(),
                        req.contact()));

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(user1.getId().id()))
                .hasPathSatisfying("$.loginId", p -> p.assertThat().isEqualTo(user1.getLoginId()))
                .hasPathSatisfying("$.password", p -> p.assertThat().isEqualTo(req.password()))
                .hasPathSatisfying("$.userType", p -> p.assertThat().isEqualTo(user1.getUserType().name()))
                .hasPathSatisfying("$.userName", p -> p.assertThat().isEqualTo(req.userName()))
                .hasPathSatisfying("$.phoneNumber", p -> p.assertThat().isEqualTo(req.phoneNumber()))
                .hasPathSatisfying("$.contact", p -> p.assertThat().isEqualTo(req.contact()));
    }

    @Test
    @WithMockUser
    void testUpdateOwnProfileOnParameterError() throws Exception {

        // given
        UserProfileUpdateRequest req = UserProfileUpdateRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました",
                        "password",
                        "userName");
        verify(profileService, never()).updateOwnProfile(any());
    }

    @Test
    @WithMockUser
    void testUpdateOwnProfileOnNotFound() throws Exception {

        // given
        UserProfileUpdateRequest req = createUserProfileUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        UserProfileUpdateCommand shouldBePassed = req.toCommand();
        when(profileService.updateOwnProfile(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyText()
                .contains("NOT_FOUND");
    }

    @Test
    void testUpdateOwnProfileOnAuthenticationError() throws Exception {

        // given
        UserProfileUpdateRequest req = createUserProfileUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private UserProfileUpdateRequest createUserProfileUpdateRequest() {
        return UserProfileUpdateRequest.builder()
                .password("newPass")
                .userName("Updated User")
                .phoneNumber("111222333")
                .contact("updated@example.com")
                .build();
    }
}
