package io.extact.msa.spring.rms.interfaces.webapi.universal;

import static io.extact.msa.spring.rms.testutils.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.application.universal.UserProfileService;
import io.extact.msa.spring.rms.application.universal.UserProfileUpdateCommand;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;

@WebMvcTest(UserProfileController.class)
@ActiveProfiles("test")
class UserProfileControllerTest {

    private static final UserCreatable testCreator = new UserCreatable() {};

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private UserProfileService profileService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class })
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
        when(profileService.getOwnProfile())
                .thenReturn(user1);

        // when
        mockMvc.perform(get("/profiles/own"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user1.getId().id()))
                .andExpect(jsonPath("$.loginId").value(user1.getLoginId()))
                .andExpect(jsonPath("$.password").value(user1.getPassword()))
                .andExpect(jsonPath("$.userType").value(user1.getUserType().name()))
                .andExpect(jsonPath("$.userName").value(user1.getProfile().getUserName()))
                .andExpect(jsonPath("$.phoneNumber").value(user1.getProfile().getPhoneNumber()))
                .andExpect(jsonPath("$.contact").value(user1.getProfile().getContact()));
    }

    @Test
    @WithMockUser
    void testGetOwnProfileOnNotFound() throws Exception {

        // given
        when(profileService.getOwnProfile())
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        mockMvc.perform(get("/profiles/own"))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testGetOwnProfileOnAuthenticationError() throws Exception {
        // given
        // @WithMockUserなし

        // when
        mockMvc.perform(get("/profiles/own"))
                // then
                .andExpect(status().isUnauthorized());

        // then
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
        mockMvc.perform(put("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user1.getId().id()))
                .andExpect(jsonPath("$.loginId").value(user1.getLoginId()))
                .andExpect(jsonPath("$.password").value(req.password()))
                .andExpect(jsonPath("$.userType").value(user1.getUserType().name()))
                .andExpect(jsonPath("$.userName").value(req.userName()))
                .andExpect(jsonPath("$.phoneNumber").value(req.phoneNumber()))
                .andExpect(jsonPath("$.contact").value(req.contact()));
    }

    @Test
    @WithMockUser
    void testUpdateOwnProfileOnParameterError() throws Exception {

        // given
        UserProfileUpdateRequest req = UserProfileUpdateRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(put("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("password"),
                        containsString("userName") //
                )));

        // then
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
        mockMvc.perform(put("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testUpdateOwnProfileOnAuthenticationError() throws Exception {

        // given
        UserProfileUpdateRequest req = createUserProfileUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(put("/profiles/own")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());
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
