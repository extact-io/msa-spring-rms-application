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
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.boundary.webapi.WebSecurityConfig;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;

@WebMvcTest(ReservationAdminController.class)
@Import(WebSecurityConfig.class)
class ReservationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ReservationAdminService reservationService;

    private static final ReservationAdminResponse reservation1 = new ReservationAdminResponse(
            1, null, null, "Test Note 1", 101, 201, null, null);
    private static final ReservationAdminResponse reservation2 = new ReservationAdminResponse(
            2, null, null, "Test Note 2", 102, 202, null, null);

    @Test
    void testGetAll() throws Exception {
        when(reservationService.getAll()).thenReturn(List.of(reservation1, reservation2));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].note").value("Test Note 1"));
    }

    @Test
    void testGetAllReturnEmpty() throws Exception {
        when(reservationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isUnauthorized());

        verify(reservationService, never()).getAll();
    }

    @Test
    void testUpdate() throws Exception {
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .id(1)
                .note("Updated Note")
                .build();
        ReservationAdminResponse updatedResponse = new ReservationAdminResponse(
                1, null, null, "Updated Note", 101, 201, null, null);

        when(reservationService.update(request.toCommand())).thenReturn(updatedResponse);

        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(put("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.note").value("Updated Note"));
    }

    @Test
    void testUpdateOnParameterError() throws Exception {
        ReservationUpdateRequest request = ReservationUpdateRequest.builder().build();
        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(put("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("パラメーターエラーが発生しました")));

        verify(reservationService, never()).update(any());
    }

    @Test
    void testUpdateOnNotFound() throws Exception {
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .id(999)
                .note("Not Found Note")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        when(reservationService.update(request.toCommand()))
                .thenThrow(new BusinessFlowException("Not found", CauseType.NOT_FOUND));

        mockMvc.perform(put("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testUpdateOnDuplicate() throws Exception {
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .id(1)
                .note("Duplicate Note")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        when(reservationService.update(request.toCommand()))
                .thenThrow(new BusinessFlowException("Duplicate", CauseType.DUPLICATE));

        mockMvc.perform(put("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
    }

    @Test
    void testUpdateOnAuthenticationError() throws Exception {
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .id(1)
                .note("Unauthorized Update")
                .build();
        String requestBody = mapper.writeValueAsString(request);

        mockMvc.perform(put("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDelete() throws Exception {
        int reservationId = 1;
        doNothing().when(reservationService).delete(new ReservationId(reservationId));

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteOnParameterError() throws Exception {
        int invalidId = -1;

        mockMvc.perform(delete("/reservations/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("パラメーターエラーが発生しました")));
    }

    @Test
    void testDeleteOnNotFound() throws Exception {
        int reservationId = 999;

        doThrow(new BusinessFlowException("Not found", CauseType.NOT_FOUND))
                .when(reservationService).delete(new ReservationId(reservationId));

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {
        int reservationId = 1;

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isUnauthorized());
    }
}
