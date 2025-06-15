package io.extact.msa.spring.rms.webapi.universal;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.rms.application.universal.LoginFailedException;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.webapi.WebApiConfig.WebApiContextConfigs;
import io.extact.msa.spring.rms.webapi.universal.LoginController;
import io.extact.msa.spring.rms.webapi.universal.LoginRequest;

@WebMvcTest
@ActiveProfiles("test")
class LoginControllerTest {

    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private LoginService loginService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            WebApiContextConfigs.class
    })
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
                .thenReturn(user1);

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/login")
                .param("loginId", loginId)
                .param("password", password)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk();
        assertThat(result)
                .headers()
                .hasEntrySatisfying(AUTHORIZATION, v -> assertThat(v)
                        .element(0)
                        .asString()
                        .matches("^Bearer .+$"));
        assertThat(result)
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
    void testLoginForGetOnFail() throws Exception {
        // given
        String loginId = "errorId";
        String password = "errorPass";
        when(loginService.login(loginId, password))
                .thenThrow(new LoginFailedException("from mock"));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/login")
                .param("loginId", loginId)
                .param("password", password)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .doesNotContainHeader(AUTHORIZATION);
    }

    @Test
    void testLoginForGetOnParameterError() throws Exception {
        // given
        String loginId = "12345678901"; // 桁数オーバー
        String password = "12345678901"; // 桁数オーバー

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/login")
                .param("loginId", loginId)
                .param("password", password)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "loginId", "password");
        verify(loginService, never()).login(any(), any());
    }

    @Test
    void testLoginForPost() throws Exception {
        // given
        String loginId = "loginId";
        String password = "password";
        LoginRequest req = new LoginRequest(loginId, password);
        String body = mapper.writeValueAsString(req);
        when(loginService.login(loginId, password))
                .thenReturn(user1);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk();
        assertThat(result)
                .headers()
                .hasEntrySatisfying(AUTHORIZATION, v -> assertThat(v)
                        .element(0)
                        .asString()
                        .isNotBlank());
        assertThat(result)
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
    void testLoginForPostOnFail() throws Exception {
        // given
        String loginId = "errorId";
        String password = "errorPass";
        LoginRequest req = new LoginRequest(loginId, password);
        String body = mapper.writeValueAsString(req);
        when(loginService.login(loginId, password))
                .thenThrow(new LoginFailedException("from mock"));

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .doesNotContainHeader(AUTHORIZATION);
    }

    @Test
    void testLoginForPostOnParameterError() throws Exception {
        // given
        String loginId = "12345678901"; // 桁数オーバー
        String password = "12345678901"; // 桁数オーバー
        LoginRequest req = new LoginRequest(loginId, password);
        String body = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "loginId", "password");
        verify(loginService, never()).login(any(), any());
    }
}
