package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static io.extact.msa.spring.rms.test.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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
import io.extact.msa.spring.rms.application.admin.UserAddCommand;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.admin.UserUpdateCommand;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;

@WebMvcTest(UserAdminController.class)
@ActiveProfiles("test")
class UserAdminControllerTest {

    private static final UserCreatable testCreator = new UserCreatable() {};

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private UserAdminService userService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class })
    static class TestConfig {
        @Bean
        UserAdminController itemAdminController(UserAdminService service) {
            return new UserAdminController(service);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAll() throws Exception {

        // given
        when(userService.getAll())
                .thenReturn(List.of(user1, user2));

        // when
        mockMvc.perform(get("/admin/users"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(user1.getId().id()))
                .andExpect(jsonPath("$[0].loginId").value(user1.getLoginId()))
                .andExpect(jsonPath("$[0].password").value(user1.getPassword()))
                .andExpect(jsonPath("$[0].userType").value(user1.getUserType().name()))
                .andExpect(jsonPath("$[0].userName").value(user1.getProfile().getUserName()))
                .andExpect(jsonPath("$[0].phoneNumber").value(user1.getProfile().getPhoneNumber()))
                .andExpect(jsonPath("$[0].contact").value(user1.getProfile().getContact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnEmpty() throws Exception {

        // given
        when(userService.getAll()).thenReturn(List.of());

        // when
        mockMvc.perform(get("/admin/users"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "ADMIN")なし

        // when
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());

        // then
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
        mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.loginId").value(req.loginId()))
                .andExpect(jsonPath("$.password").value(req.password()))
                .andExpect(jsonPath("$.userType").value(req.userType().name()))
                .andExpect(jsonPath("$.userName").value(req.userName()))
                .andExpect(jsonPath("$.phoneNumber").value(req.phoneNumber()))
                .andExpect(jsonPath("$.contact").value(req.contact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddOnParameterError() throws Exception {

        // given
        UserAddRequest req = UserAddRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("loginId"),
                        containsString("password"),
                        containsString("userType") //
                )));

        // then
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
        mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
    }

    @Test
    void testAddOnAuthenticationError() throws Exception {

        // given
        UserAddRequest req = createUserAddRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
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
        mockMvc.perform(put("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(req.id()))
                .andExpect(jsonPath("$.loginId").value(paddingloginId))
                .andExpect(jsonPath("$.password").value(req.password()))
                .andExpect(jsonPath("$.userType").value(req.userType().name()))
                .andExpect(jsonPath("$.userName").value(req.userName()))
                .andExpect(jsonPath("$.phoneNumber").value(req.phoneNumber()))
                .andExpect(jsonPath("$.contact").value(req.contact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnParameterError() throws Exception {

        // given
        UserUpdateRequest req = UserUpdateRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(put("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("password"),
                        containsString("userType") //
                )));

        // then
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
        mockMvc.perform(put("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testUpdateOnAuthenticationError() throws Exception {

        // given
        UserUpdateRequest req = createUserUpdateRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(put("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete() throws Exception {

        // given
        int userId = 1;
        doNothing().when(userService).delete(new UserId(userId));

        // when
        mockMvc.perform(delete("/admin/users/{id}", userId))
                // then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnParameterError() throws Exception {

        // given
        int invalidId = -1;

        // when
        mockMvc.perform(delete("/admin/users/{id}", invalidId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"), //
                        containsString("id"))));

        // then
        verify(userService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnNotFound() throws Exception {

        // given
        int userId = 999;
        doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(userService).delete(new UserId(userId));

        // when
        mockMvc.perform(delete("/admin/users/{id}", userId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {

        // given
        int userId = 1;

        // when
        mockMvc.perform(delete("/admin/users/{id}", userId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
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
