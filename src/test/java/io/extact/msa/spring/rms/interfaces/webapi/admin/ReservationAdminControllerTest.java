package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationUpdateCommand;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationUpdateRequest.ReservationUpdateRequestBuilder;

@WebMvcTest(ReservationAdminController.class)
@ActiveProfiles("test")
class ReservationAdminControllerTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {
    };
    private static DateTimeFormatter dateTimeFormatter;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private ReservationAdminService reservationService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class })
    static class TestConfig {
        @Bean
        ReservationAdminController reservationAdminController(ReservationAdminService service) {
            return new ReservationAdminController(service);
        }
    }

    @BeforeAll
    static void beforeAll(
            @Value("${rms.persistence.reservation.remote.format.date-time}") //
            String dateTimePattern) {
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAll() throws Exception {

        // given
        when(reservationService.getAll())
                .thenReturn(List.of(model1, model2, model3));

        // when
        mockMvc.perform(get("/admin/reservations"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fromDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .andExpect(jsonPath("$[0].toDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .andExpect(jsonPath("$[0].note").value(model1.reservation().getNote()))
                .andExpect(jsonPath("$[0].itemId").value(model1.reservation().getItemId().id()))
                .andExpect(jsonPath("$[0].reserverId").value(model1.reservation().getReserverId().id()))
                .andExpect(jsonPath("$[0].item.id").value(model1.rentalItem().getId().id()))
                .andExpect(jsonPath("$[0].item.serialNo").value(model1.rentalItem().getSerialNo()))
                .andExpect(jsonPath("$[0].item.itemName").value(model1.rentalItem().getItemName()))
                .andExpect(jsonPath("$[0].reserver.id").value(model1.reserver().getId().id()))
                .andExpect(jsonPath("$[0].reserver.loginId").value(model1.reserver().getLoginId()))
                .andExpect(jsonPath("$[0].reserver.password").value(model1.reserver().getPassword()))
                .andExpect(jsonPath("$[0].reserver.userType").value(model1.reserver().getUserType().name()))
                .andExpect(jsonPath("$[0].reserver.userName").value(model1.reserver().getProfile().getUserName()))
                .andExpect(jsonPath("$[0].reserver.phoneNumber").value(model1.reserver().getProfile().getPhoneNumber()))
                .andExpect(jsonPath("$[0].reserver.contact").value(model1.reserver().getProfile().getContact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnEmpty() throws Exception {

        // given
        when(reservationService.getAll())
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/admin/reservations"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "ADMIN")なし

        // when
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdate() throws Exception {

        // given
        ReservationUpdateRequest req = createReservationUpdateRequestBuilder().build();
        String requestBody = mapper.writeValueAsString(req);

        ReservationUpdateCommand shouldBePassed = req.toCommand();
        when(reservationService.update(shouldBePassed))
                .thenReturn(new ReservationComposeModel(
                        testCreator.newInstance(
                                reservation2.getId(),
                                new ReservationPeriod(req.fromDateTime(), req.toDateTime()),
                                req.note(),
                                reservation2.getItemId(),
                                reservation2.getReserverId()),
                        item3,
                        user2));

        // when
        mockMvc.perform(put("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(req.id()))
                .andExpect(jsonPath("$.fromDateTime").value(dateTimeFormatter.format(req.fromDateTime())))
                .andExpect(jsonPath("$.toDateTime").value(dateTimeFormatter.format(req.toDateTime())))
                .andExpect(jsonPath("$.note").value(req.note()))
                .andExpect(jsonPath("$.itemId").value(reservation2.getItemId().id()))
                .andExpect(jsonPath("$.reserverId").value(reservation2.getReserverId().id()))
                .andExpect(jsonPath("$.item.id").value(item3.getId().id()))
                .andExpect(jsonPath("$.item.serialNo").value(item3.getSerialNo()))
                .andExpect(jsonPath("$.item.itemName").value(item3.getItemName()))
                .andExpect(jsonPath("$.reserver.id").value(user2.getId().id()))
                .andExpect(jsonPath("$.reserver.loginId").value(user2.getLoginId()))
                .andExpect(jsonPath("$.reserver.password").value(user2.getPassword()))
                .andExpect(jsonPath("$.reserver.userType").value(user2.getUserType().name()))
                .andExpect(jsonPath("$.reserver.userName").value(user2.getProfile().getUserName()))
                .andExpect(jsonPath("$.reserver.phoneNumber").value(user2.getProfile().getPhoneNumber()))
                .andExpect(jsonPath("$.reserver.contact").value(user2.getProfile().getContact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnParameterError() throws Exception {

        // given
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(request);

        // when
        mockMvc.perform(put("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("id"),
                        containsString("fromDateTime"),
                        containsString("toDateTime") //
                )));

        // then
        verify(reservationService, never()).update(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnNotFound() throws Exception {

        // given
        ReservationUpdateRequest req = createReservationUpdateRequestBuilder().build();
        String requestBody = mapper.writeValueAsString(req);

        ReservationUpdateCommand shouldBePassed = req.toCommand();
        when(reservationService.update(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        mockMvc.perform(put("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnDuplicate() throws Exception {

        // given
        ReservationUpdateRequest req = createReservationUpdateRequestBuilder().build();
        String requestBody = mapper.writeValueAsString(req);

        ReservationUpdateCommand shouldBePassed = req.toCommand();
        when(reservationService.update(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        mockMvc.perform(put("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
    }

    @Test
    void testUpdateOnAuthenticationError() throws Exception {

        // given
        ReservationUpdateRequest req = createReservationUpdateRequestBuilder().build();
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(put("/admin/reservations")
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
        int reservationId = 1;
        doNothing().when(reservationService).delete(new ReservationId(reservationId));

        // when
        mockMvc.perform(delete("/admin/reservations/{id}", reservationId))
                // then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnParameterError() throws Exception {

        // given
        int invalidId = -1;

        // when
        mockMvc.perform(delete("/admin/reservations/{id}", invalidId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"), //
                        containsString("id"))));

        // then
        verify(reservationService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnNotFound() throws Exception {

        // given
        int reservationId = 999;
        doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(reservationService).delete(new ReservationId(reservationId));

        // when
        mockMvc.perform(delete("/admin/reservations/{id}", reservationId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {

        // given
        int reservationId = 1;

        // when
        mockMvc.perform(delete("/admin/reservations/{id}", reservationId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).delete(any());
    }

    private ReservationUpdateRequestBuilder createReservationUpdateRequestBuilder() {
        return ReservationUpdateRequest.builder()
                .id(2)
                .fromDateTime(LocalDateTime.of(2024, 1, 1, 9, 0))
                .toDateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .note("Updated Note");
    }
}
