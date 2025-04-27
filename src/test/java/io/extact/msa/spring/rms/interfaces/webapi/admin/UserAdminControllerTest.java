package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import io.extact.msa.spring.rms.application.admin.UserAddCommand;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.admin.UserUpdateCommand;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.interfaces.webapi.WebApiConfig.WebApiContextConfigs;

@WebMvcTest
@ActiveProfiles("test")
class UserAdminControllerTest {

    private static final UserCreatable testCreator = new UserCreatable() {
    };

    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private UserAdminService userService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            WebApiContextConfigs.class
    })
    static class TestConfig {
        @Bean
        UserAdminController userAdminController(UserAdminService service) {
            return new UserAdminController(service);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAll() throws Exception {

        // given
        when(userService.getAll()).thenReturn(List.of(user1, user2));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/users")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(2))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(user1.getId().id()))
                .hasPathSatisfying("$[0].loginId", p -> p.assertThat().isEqualTo(user1.getLoginId()))
                .hasPathSatisfying("$[0].password", p -> p.assertThat().isEqualTo(user1.getPassword()))
                .hasPathSatisfying("$[0].userType", p -> p.assertThat().isEqualTo(user1.getUserType().name()))
                .hasPathSatisfying("$[0].userName", p -> p.assertThat().isEqualTo(user1.getProfile().getUserName()))
                .hasPathSatisfying("$[0].phoneNumber",
                        p -> p.assertThat().isEqualTo(user1.getProfile().getPhoneNumber()))
                .hasPathSatisfying("$[0].contact", p -> p.assertThat().isEqualTo(user1.getProfile().getContact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnEmpty() throws Exception {

        // given
        when(userService.getAll()).thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/users")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/users")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(userService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdd() throws Exception {

        // given
        UserAddRequest req = createUserAddRequest();
        String requestBody = mapper.writeValueAsString(req);

        UserAddCommand shouldBePassed = req.toCommand();
        when(userService.add(shouldBePassed))
                .thenReturn(testCreator.newInstance(
                        new UserId(2),
                        req.loginId(),
                        req.password(),
                        req.userType(),
                        req.userName(),
                        req.phoneNumber(),
                        req.contact()));

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.loginId", p -> p.assertThat().isEqualTo(req.loginId()))
                .hasPathSatisfying("$.password", p -> p.assertThat().isEqualTo(req.password()))
                .hasPathSatisfying("$.userType", p -> p.assertThat().isEqualTo(req.userType().name()))
                .hasPathSatisfying("$.userName", p -> p.assertThat().isEqualTo(req.userName()))
                .hasPathSatisfying("$.phoneNumber", p -> p.assertThat().isEqualTo(req.phoneNumber()))
                .hasPathSatisfying("$.contact", p -> p.assertThat().isEqualTo(req.contact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddOnParameterError() throws Exception {

        // given
        UserAddRequest req = UserAddRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました",
                        "loginId",
                        "password",
                        "userType");
        verify(userService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddOnDuplicate() throws Exception {

        // given
        UserAddRequest req = createUserAddRequest();
        String requestBody = mapper.writeValueAsString(req);

        UserAddCommand shouldBePassed = req.toCommand();
        when(userService.add(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.CONFLICT)
                .bodyText()
                .contains("DUPLICATE");
    }

    @Test
    void testAddOnAuthenticationError() throws Exception {

        // given
        UserAddRequest req = createUserAddRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(userService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdate() throws Exception {

        // given
        UserUpdateRequest req = createUserUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        String paddingloginId = "loginId";
        UserUpdateCommand shouldBePassed = req.toCommand();
        when(userService.update(shouldBePassed))
                .thenReturn(testCreator.newInstance(
                        new UserId(req.id()),
                        paddingloginId,
                        req.password(),
                        req.userType(),
                        req.userName(),
                        req.phoneNumber(),
                        req.contact()));

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(req.id()))
                .hasPathSatisfying("$.loginId", p -> p.assertThat().isEqualTo(paddingloginId))
                .hasPathSatisfying("$.password", p -> p.assertThat().isEqualTo(req.password()))
                .hasPathSatisfying("$.userType", p -> p.assertThat().isEqualTo(req.userType().name()))
                .hasPathSatisfying("$.userName", p -> p.assertThat().isEqualTo(req.userName()))
                .hasPathSatisfying("$.phoneNumber", p -> p.assertThat().isEqualTo(req.phoneNumber()))
                .hasPathSatisfying("$.contact", p -> p.assertThat().isEqualTo(req.contact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnParameterError() throws Exception {

        // given
        UserUpdateRequest req = UserUpdateRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました",
                        "password",
                        "userType");
        verify(userService, never()).update(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnNotFound() throws Exception {

        // given
        UserUpdateRequest req = createUserUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        UserUpdateCommand shouldBePassed = req.toCommand();
        when(userService.update(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/users")
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
    void testUpdateOnAuthenticationError() throws Exception {

        // given
        UserUpdateRequest req = createUserUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(userService, never()).update(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete() throws Exception {

        // given
        int userId = 1;
        Mockito.doNothing().when(userService).delete(new UserId(userId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/users/{id}", userId)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnParameterError() throws Exception {

        // given
        int invalidId = -1;

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/users/{id}", invalidId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "id");
        verify(userService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnNotFound() throws Exception {

        // given
        int userId = 999;
        Mockito.doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(userService).delete(new UserId(userId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/users/{id}", userId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyText()
                .contains("NOT_FOUND");
    }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {

        // given
        int userId = 1;

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/users/{id}", userId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(userService, never()).delete(any());
    }

    private UserAddRequest createUserAddRequest() {
        return UserAddRequest.builder()
                .loginId("login1")
                .password("password1")
                .userType(UserType.ADMIN)
                .userName("User One")
                .phoneNumber("123456789")
                .contact("user1@example.com")
                .build();
    }

    private UserUpdateRequest createUserUpdateRequest() {
        return UserUpdateRequest.builder()
                .id(1)
                .password("newPass")
                .userType(UserType.MEMBER)
                .userName("Updated User")
                .phoneNumber("111222333")
                .contact("updated@example.com")
                .build();
    }
}
