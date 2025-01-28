package io.extact.msa.spring.rms.interfaces.webapi.member;

import static io.extact.msa.spring.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
import io.extact.msa.spring.rms.application.member.ItemReservationService;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationUpdateRequest.ReservationUpdateRequestBuilder;

@WebMvcTest(ItemReservationController.class)
class ReservationAdminControllerTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {};

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemReservationService reservationService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class })
    static class TestConfig {
        @Bean
        ItemReservationController reservationMemberController(ItemReservationService service) {
            return new ItemReservationController(service);
        }
    }

    @Test
    @WithMockUser
    void testGetItemAll() throws Exception {

        // given
        when(reservationService.getItemAll())
            .thenReturn(List.of(item1, item2, item3, item4));

        // when
        mockMvc.perform(get("/member/items"))
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
        when(reservationService.getItemAll())
            .thenReturn(List.of());

        // when
        mockMvc.perform(get("/member/items"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetItemAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし

        // when
        mockMvc.perform(get("/member/items"))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).getItemAll();
    }

    @Test
    @WithMockUser
    void testFindRentableItemAtPeriod() throws Exception {

        // given
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.findRentableItemAtPeriod(from, to))
            .thenReturn(List.of(item2, item4));

        // when
        mockMvc.perform(get("/member/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(item1.getId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(item1.getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(item1.getItemName())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser
    void testFindRentableItemAtPeriodReturnEmpty() throws Exception {

        // given
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.findRentableItemAtPeriod(from, to))
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/member/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void testFindRentableItemAtPeriodOnParameterError() throws Exception {

        // given
        // when
        mockMvc.perform(get("/member/rentable")
                .param("from", ""))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("from"),
                        containsString("to") //
                )));

        // then
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }

    @Test
    void testFindRentableItemAtPeriodOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);

        // when
        mockMvc.perform(get("/member/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }


    @Test
    @WithMockUser
    void testIsRentableItemAtPeriod() throws Exception {

        // given
        ItemId itemid = new ItemId(1);
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.isRentableItemAtPeriod(itemid, from, to))
            .thenReturn(false);

        // when
        mockMvc.perform(get("/items/{itemId}/rentable", itemid.id())
                .param("from", from.toString())
                .param("to", to.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithMockUser
    void testFindRentableItemAtPeriodReturnEmpty() throws Exception {

        // given
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.findRentableItemAtPeriod(from, to))
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/member/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void testFindRentableItemAtPeriodOnParameterError() throws Exception {

        // given
        // when
        mockMvc.perform(get("/member/rentable")
                .param("from", ""))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("from"),
                        containsString("to") //
                )));

        // then
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }

    @Test
    void testFindRentableItemAtPeriodOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);

        // when
        mockMvc.perform(get("/admin/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }

    @Test
    @WithMockUser
    void testFindReservationByItemId() throws Exception {

        // given
        ItemId itemId = new ItemId(1);
        when(reservationService.findReservationByItemId(itemId))
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reservations/items/{itemId}", itemId.id()))
                // then
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(model1.reservation().getId().id()))
        // TODO コンバーターと一緒に直す
        //.andExpect(jsonPath("$[0].fromDateTime").value(model1.reservation().getPeriod().getFrom()))
        //.andExpect(jsonPath("$[0].toDateTime").value(model1.reservation().getPeriod().getTo()))
        .andExpect(jsonPath("$[0].note").value(model1.reservation().getNote()))
        .andExpect(jsonPath("$[0].itemId").value(model1.reservation().getItemId().id()))
        .andExpect(jsonPath("$[0].serialNo").value(model1.rentalItem().getSerialNo()))
        .andExpect(jsonPath("$[0].itemName").value(model1.rentalItem().getItemName()))
        .andExpect(jsonPath("$[0].reserverId").value(model1.reservation().getReserverId().id())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser
    void testFindReservationByItemIdWithFromDate() throws Exception {

        // given
        ItemId itemId = new ItemId(1);
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        when(reservationService.findReservationByItemIdAndFromDate(itemId, fromDate))
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reservations/items/{itemId}", itemId.id())
                .param("from-date", fromDate.toString()))
                // then
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(model1.reservation().getId().id()))
                // TODO コンバーターと一緒に直す
                //.andExpect(jsonPath("$[0].fromDateTime").value(model1.reservation().getPeriod().getFrom()))
                //.andExpect(jsonPath("$[0].toDateTime").value(model1.reservation().getPeriod().getTo()))
                .andExpect(jsonPath("$[0].note").value(model1.reservation().getNote()))
                .andExpect(jsonPath("$[0].itemId").value(model1.reservation().getItemId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(model1.rentalItem().getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(model1.rentalItem().getItemName()))
                .andExpect(jsonPath("$[0].reserverId").value(model1.reservation().getReserverId().id())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser
    void testFindReservationByItemIdWithFromDateNull() throws Exception {

        // given
        ItemId itemId = new ItemId(1);
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        when(reservationService.findReservationByItemIdAndFromDate(itemId, fromDate))
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reservations/items/{itemId}", itemId.id())
                .param("from-date", ""))
                // then
                .andExpect(jsonPath("$.length()").value(2)); // 以降省略
    }

    @Test
    @WithMockUser
    void testFindReservationByItemIdReturnEmpty() throws Exception {

        // given
        ItemId itemId = new ItemId(1);
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        when(reservationService.findReservationByItemIdAndFromDate(itemId, fromDate))
            .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reservations/items/{itemId}", itemId.id())
                .param("from-date", fromDate.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void testFindReservationByItemIdOnParameterError() throws Exception {

        // given
        ItemId itemId = new ItemId(-1);

        // when
        mockMvc.perform(get("/reservations/items/{itemId}", itemId.id()))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("itemId") //
                )));

        // then
        verify(reservationService, never()).findReservationByItemId(any());
    }

    @Test
    void testFindReservationByItemIdOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし
        ItemId itemId = new ItemId(-1);

        // when
        mockMvc.perform(get("/reservations/items/{itemId}", itemId.id()))
                // then
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).findReservationByItemId(any());
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
