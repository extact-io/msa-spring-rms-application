package io.extact.msa.spring.rms.boundary.webapi.admin;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.boundary.webapi.WebSecurityConfig;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserProfile;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.domain.user.model.UserType;

@WebMvcTest(UserAdminController.class)
@Import(WebSecurityConfig.class)
class UserAdminControllerTest {

    private static final UserCreatable testCreator = new UserCreatable() {};

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserAdminService userService;

    private static final UserReference user1 = new UserReference(
            new UserId(1), "login1", "password1", UserType.ADMIN,
            new UserProfile("User One", "123456789", "user1@example.com"));

    private static final UserReference user2 = new UserReference(
            new UserId(2), "login2", "password2", UserType.MEMBER,
            new UserProfile("User Two", "987654321", "user2@example.com"));

    @Test
    void testGetAll() throws Exception {
        when(userService.getAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].loginId").value("login1"));
    }

    @Test
    void testGetAllReturnEmpty() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getAll();
    }

    @Test
    void testAdd() throws Exception {
        UserAddRequest request = UserAddRequest.builder()
                .loginId("login1")
                .password("password1")
                .userType(UserType.ADMIN)
                .userName("User One")
                .phoneNumber("123456789")
                .contact("user1@example.com")
                .build();
        when(userService.add(request.toCommand())).thenReturn(user1);

        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testAddOnParameterError() throws Exception {
        UserAddRequest request = UserAddRequest.builder().build();
        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("パラメーターエラーが発生しました")));

        verify(userService, never()).add(any());
    }

    @Test
    void testAddOnDuplicate() throws Exception {
        UserAddRequest request = UserAddRequest.builder()
                .loginId("login1")
                .password("password1")
                .userType(UserType.ADMIN)
                .userName("User One")
                .phoneNumber("123456789")
                .contact("user1@example.com")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        when(userService.add(request.toCommand()))
                .thenThrow(new BusinessFlowException("Duplicate user", CauseType.DUPLICATE));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
    }

    @Test
    void testAddOnAuthenticationError() throws Exception {
        UserAddRequest request = UserAddRequest.builder()
                .loginId("login1")
                .password("password1")
                .userType(UserType.ADMIN)
                .userName("User One")
                .phoneNumber("123456789")
                .contact("user1@example.com")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdate() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(1)
                .password("newPassword")
                .userType(UserType.MEMBER)
                .userName("Updated User")
                .phoneNumber("111222333")
                .contact("updated@example.com")
                .build();
        when(userService.update(request.toCommand())).thenReturn(user1);

        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateOnParameterError() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .password("invalid")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("パラメーターエラーが発生しました")));
    }

    @Test
    void testUpdateOnNotFound() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(999)
                .password("password")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        when(userService.update(request.toCommand()))
                .thenThrow(new BusinessFlowException("Not found", CauseType.NOT_FOUND));

        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testUpdateOnDuplicate() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(1)
                .password("duplicate")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        when(userService.update(request.toCommand()))
                .thenThrow(new BusinessFlowException("Duplicate", CauseType.DUPLICATE));

        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
    }

    @Test
    void testUpdateOnAuthenticationError() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .id(1)
                .password("password")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDelete() throws Exception {
        int userId = 1;
        doNothing().when(userService).delete(new UserId(userId));

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteOnParameterError() throws Exception {
        int invalidId = -1;

        mockMvc.perform(delete("/users/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("パラメーターエラーが発生しました")));
    }

    @Test
    void testDeleteOnNotFound() throws Exception {
        int userId = 999;

        doThrow(new BusinessFlowException("Not found", CauseType.NOT_FOUND))
                .when(userService).delete(new UserId(userId));

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {
        int userId = 1;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isUnauthorized());
    }
}
