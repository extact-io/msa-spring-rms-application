package io.extact.msa.spring.rms.interfaces.webapi.member;

import static io.extact.msa.spring.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.web.RestControllerConfig;
import io.extact.msa.spring.rms.application.admin.ReservationUpdateCommand;
import io.extact.msa.spring.rms.application.member.ReservationMemberService;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationUpdateRequest.ReservationUpdateRequestBuilder;

@WebMvcTest(ReservationMemberController.class)
class ReservationAdminControllerTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {};

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ReservationMemberService reservationService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class })
    static class TestConfig {
        @Bean
        ReservationMemberController reservationMemberController(ReservationMemberService service) {
            return new ReservationMemberController(service);
        }
    }

    @Test
    @WithMockUser
    void testGetItemAll() throws Exception {

        // given
        when(reservationService.getItemAll())
            .thenReturn(List.of(item1, item2, item3, item4));

        // when
        mockMvc.perform(get("/member/reservations"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(item1.getId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(item1.getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(item1.getItemName())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser
    void testGetItemAllReturnEmpty() throws Exception {

        // given
        when(reservationService.getItemAll()))
            .thenReturn(List.of());

        // when
        mockMvc.perform(get("/member/reservations"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし

        // when
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).getAll();
    }

    @Test
    @WithMockUser
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
                // TODO 書式と精度はコンバーターと一緒に直す
                //.andExpect(jsonPath("$.fromDateTime").value(req.fromDateTime()))
                //.andExpect(jsonPath("$.toDateTime").value(req.toDateTime()))
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
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
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
