package io.extact.msa.spring.rms.interfaces.webapi.universal;

import static io.extact.msa.spring.rms.testutils.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.core.jwt.encode.JwtEncodeConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;
import io.extact.msa.spring.rms.testutils.PersistedTestData;

@WebMvcTest(LoginController.class)
@ActiveProfiles("test")
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private LoginService loginService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class,
            JwtEncodeConfig.class })
    static class TestConfig {
        @Bean
        LoginController loginController(LoginService service) {
            return new LoginController(service);
        }
    }

    @Test
    void testLoginForGet() throws Exception {

        // given
        String loginId = "loginId";
        String password = "password";
        when(loginService.login(loginId, password))
                .thenReturn(PersistedTestData.user1);

        // when
        mockMvc.perform(get("/login")
                .param("loginId", loginId)
                .param("password", password))
                // then
                .andExpect(status().isOk())
                .andExpect(header().string(AUTHORIZATION, not(blankOrNullString())))
                .andExpect(jsonPath("$.id").value(user1.getId().id()))
                .andExpect(jsonPath("$.loginId").value(user1.getLoginId()))
                .andExpect(jsonPath("$.password").value(user1.getPassword()))
                .andExpect(jsonPath("$.userType").value(user1.getUserType().name()))
                .andExpect(jsonPath("$.userName").value(user1.getProfile().getUserName()))
                .andExpect(jsonPath("$.phoneNumber").value(user1.getProfile().getPhoneNumber()))
                .andExpect(jsonPath("$.contact").value(user1.getProfile().getContact()));
    }

    @Test
    void testLoginForGetOnFail() throws Exception {

        // given
        String loginId = "errorId";
        String password = "errorPass";
        when(loginService.login(loginId, password))
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        mockMvc.perform(get("/login")
                .param("loginId", loginId)
                .param("password", password))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(header().doesNotExist(AUTHORIZATION))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testLoginForGetOnParameterError() throws Exception {

        // given
        String loginId = "12345678901"; // 桁数オーバー
        String password = "12345678901"; // 桁数オーバー

        // when
        mockMvc.perform(get("/login")
                .param("loginId", loginId)
                .param("password", password))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("loginId"),
                        containsString("password") //
                )));

        // then
        verify(loginService, never()).login(any(), any());
    }

    @Test
    void testLoginForPost() throws Exception {

        // given
        String loginId = "loginId";
        String password = "password";
        LoginRequest req = new LoginRequest(loginId, password);
        String requestBody = mapper.writeValueAsString(req);

        when(loginService.login(loginId, password))
                .thenReturn(PersistedTestData.user1);

        // when
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andExpect(status().isOk())
                .andExpect(header().string(AUTHORIZATION, not(blankOrNullString())))
                .andExpect(jsonPath("$.id").value(user1.getId().id()))
                .andExpect(jsonPath("$.loginId").value(user1.getLoginId()))
                .andExpect(jsonPath("$.password").value(user1.getPassword()))
                .andExpect(jsonPath("$.userType").value(user1.getUserType().name()))
                .andExpect(jsonPath("$.userName").value(user1.getProfile().getUserName()))
                .andExpect(jsonPath("$.phoneNumber").value(user1.getProfile().getPhoneNumber()))
                .andExpect(jsonPath("$.contact").value(user1.getProfile().getContact()));
    }

    @Test
    void testLoginForPostOnFail() throws Exception {

        // given
        String loginId = "errorId";
        String password = "errorPass";
        LoginRequest req = new LoginRequest(loginId, password);
        String requestBody = mapper.writeValueAsString(req);

        when(loginService.login(loginId, password))
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(header().doesNotExist(AUTHORIZATION))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testLoginForPostOnParameterError() throws Exception {

        // given
        String loginId = "12345678901"; // 桁数オーバー
        String password = "12345678901"; // 桁数オーバー
        LoginRequest req = new LoginRequest(loginId, password);
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("loginId"),
                        containsString("password") //
                )));

        // then
        verify(loginService, never()).login(any(), any());
    }
}
