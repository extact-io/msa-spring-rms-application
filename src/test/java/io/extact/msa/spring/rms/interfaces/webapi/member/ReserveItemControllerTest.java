package io.extact.msa.spring.rms.interfaces.webapi.member;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
import io.extact.msa.spring.platform.fw.feature.exception.RmsRequestCheckException;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.application.member.ReserveItemCommand;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import io.extact.msa.spring.rms.application.member.ReserveItemService;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;

@WebMvcTest(ReserveItemController.class)
@ActiveProfiles("test")
class ReserveItemControllerTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {
    };
    private static DateTimeFormatter dateTimeFormatter;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private ReserveItemService reservationService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class })
    static class TestConfig {
        @Bean
        ReserveItemController reservationMemberController(ReserveItemService service) {
            return new ReserveItemController(service);
        }
    }

    @BeforeAll
    static void beforeAll(
            @Value("${rms.persistence.reservation.remote.format.date-time}") //
            String dateTimePattern) {
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testGetItemAll() throws Exception {

        // given
        when(reservationService.getItemAll())
                .thenReturn(List.of(item1, item2, item3, item4));

        // when
        mockMvc.perform(get("/reserve/items"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(item1.getId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(item1.getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(item1.getItemName())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testGetItemAllReturnEmpty() throws Exception {

        // given
        when(reservationService.getItemAll())
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reserve/items"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).getItemAll();
    }

    @Test
    public void testGetItemAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし

        // when
        mockMvc.perform(get("/reserve/items"))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).getItemAll();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindRentableItemAtPeriod() throws Exception {

        // given
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.findRentableItemAtPeriod(from, to))
                .thenReturn(List.of(item2, item4));

        // when
        mockMvc.perform(get("/reserve/items/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(item2.getId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(item2.getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(item2.getItemName())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindRentableItemAtPeriodReturnEmpty() throws Exception {

        // given
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.findRentableItemAtPeriod(from, to))
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reserve/items/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findRentableItemAtPeriod(from, to);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindRentableItemAtPeriodOnParameterError() throws Exception {

        // given
        // when
        mockMvc.perform(get("/reserve/items/rentable")
                .param("from", ""))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("from") //
                )));

        // then
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }

    @Test
    public void testFindRentableItemAtPeriodOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);

        // when
        mockMvc.perform(get("/reserve/items/rentable")
                .param("from", from.toString())
                .param("to", to.toString()))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testIsRentableItemAtPeriod() throws Exception {

        // given
        ItemId itemid = new ItemId(1);
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        boolean returnValue = false;
        when(reservationService.isRentableItemAtPeriod(itemid, from, to))
                .thenReturn(returnValue);

        // when
        mockMvc.perform(get("/reserve/items/{itemId}/rentable", itemid.id())
                .param("from", from.toString())
                .param("to", to.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(returnValue)));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testIsRentableItemAtPeriodOnPathVariableError() throws Exception {

        // given
        String itemid = "a"; // コンバートエラー
        String from = ""; // リクエストパラメータなしエラー
        String to = LocalDateTime.of(2025, 1, 1, 12, 0).toString();

        // when
        mockMvc.perform(get("/reserve/items/{itemId}/rentable", itemid)
                .param("from", from)
                .param("to", to))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("itemId") // パスパラメータのチェックで中断される
                )));

        // then
        verify(reservationService, never()).isRentableItemAtPeriod(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testIsRentableItemAtPeriodOnRequestPrameterError() throws Exception {

        // given
        String itemid = "1";
        String from = ""; // リクエストパラメータなしエラー
        String to = LocalDateTime.of(2025, 1, 1, 12, 0).toString();

        // when
        mockMvc.perform(get("/reserve/items/{itemId}/rentable", itemid)
                .param("from", from)
                .param("to", to))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("from"))));

        // then
        verify(reservationService, never()).isRentableItemAtPeriod(any(), any(), any());
    }

    @Test
    public void testIsRentableItemAtPeriodOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        ItemId itemid = new ItemId(1);
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);

        // when
        mockMvc.perform(get("/reserve/items/{itemId}/rentable", itemid.id())
                .param("from", from.toString())
                .param("to", to.toString()))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).isRentableItemAtPeriod(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByItemId() throws Exception {

        // given
        Integer itemId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("item-id", itemId.toString()))
                // then
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(model1.reservation().getId().id()))
                .andExpect(jsonPath("$[0].fromDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .andExpect(jsonPath("$[0].toDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .andExpect(jsonPath("$[0].note").value(model1.reservation().getNote()))
                .andExpect(jsonPath("$[0].itemId").value(model1.reservation().getItemId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(model1.rentalItem().getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(model1.rentalItem().getItemName()))
                .andExpect(jsonPath("$[0].reserverId").value(model1.reservation().getReserverId().id())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByItemIdWithFromDate() throws Exception {

        // given
        Integer itemId = 1;
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("item-id", itemId.toString())
                .param("from-date", fromDate.toString()))
                // then
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(model1.reservation().getId().id()))
                .andExpect(jsonPath("$[0].fromDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .andExpect(jsonPath("$[0].toDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .andExpect(jsonPath("$[0].note").value(model1.reservation().getNote()))
                .andExpect(jsonPath("$[0].itemId").value(model1.reservation().getItemId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(model1.rentalItem().getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(model1.rentalItem().getItemName()))
                .andExpect(jsonPath("$[0].reserverId").value(model1.reservation().getReserverId().id())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByItemIdWithFromDateNull() throws Exception {

        // given
        Integer itemId = 1;
        LocalDate fromDate = null;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("item-id", itemId.toString())
                .param("from-date", ""))
                // then
                .andExpect(jsonPath("$.length()").value(2)); // 以降省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByItemIdReturnEmpty() throws Exception {

        // given
        Integer itemId = 1;
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .from(fromDate)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("item-id", itemId.toString())
                .param("from-date", fromDate.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByItemIdOnParameterError() throws Exception {

        // given
        Integer invalidItemId = -1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(invalidItemId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("item-id", invalidItemId.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    public void testFindReservationByItemIdOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        Integer itemId = -1;

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("item-id", itemId.toString()))
                // then
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).findReservationByCondition(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByReserverId() throws Exception {

        // given
        Integer reserverId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(reserverId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("reserver-id", reserverId.toString()))
                // then
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(model1.reservation().getId().id()))
                .andExpect(jsonPath("$[0].fromDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .andExpect(jsonPath("$[0].toDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .andExpect(jsonPath("$[0].note").value(model1.reservation().getNote()))
                .andExpect(jsonPath("$[0].itemId").value(model1.reservation().getItemId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(model1.rentalItem().getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(model1.rentalItem().getItemName()))
                .andExpect(jsonPath("$[0].reserverId").value(model1.reservation().getReserverId().id())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByReserverIdReturnEmpty() throws Exception {

        // given
        Integer reserverId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(reserverId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("reserver-id", reserverId.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationByReserverIdOnParameterError() throws Exception {

        // given
        Integer invalidReserverId = -1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(invalidReserverId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("reserver-id", invalidReserverId.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    public void testFindReservationByReserverIdOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        Integer reserverId = -1;

        // when
        mockMvc.perform(get("/reserve/reservations")
                .param("reserver-id", reserverId.toString()))
                // then
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).findReservationByCondition(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testFindReservationOnNoParameterError() throws Exception {

        // given
        // when
        mockMvc.perform(get("/reserve/reservations"))
                // then
                .andExpect(status().isBadRequest())
                //.andExpect(header("rms-exception").);
                .andExpect(header().string("rms-exception", RmsRequestCheckException.class.getSimpleName()));

        // then
        verify(reservationService, never()).findReservationByCondition(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testGetOwnReservations() throws Exception {

        // given
        when(reservationService.getOwnReservations())
                .thenReturn(List.of(model1, model2));

        // when
        mockMvc.perform(get("/reserve/reservations/own"))
                // then
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(model1.reservation().getId().id()))
                .andExpect(jsonPath("$[0].fromDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .andExpect(jsonPath("$[0].toDateTime")
                        .value(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .andExpect(jsonPath("$[0].note").value(model1.reservation().getNote()))
                .andExpect(jsonPath("$[0].itemId").value(model1.reservation().getItemId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(model1.rentalItem().getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(model1.rentalItem().getItemName()))
                .andExpect(jsonPath("$[0].reserverId").value(model1.reservation().getReserverId().id())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testGetOwnReservationsReturnEmpty() throws Exception {

        // given
        when(reservationService.getOwnReservations())
                .thenReturn(List.of());

        // when
        mockMvc.perform(get("/reserve/reservations/own"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).getOwnReservations();
    }

    @Test
    public void testGetOwnReservationsOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし

        // when
        mockMvc.perform(get("/reserve/reservations/own"))
                // then
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).getOwnReservations();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testReserve() throws Exception {

        // given
        ReserveItemRequest req = createReserveItemRequest();
        String requestBody = mapper.writeValueAsString(req);

        ReserveItemCommand shouldBePassed = req.toCommand();
        ReservationId newId = new ReservationId(1000);
        UserId reserverId = new UserId(1);
        when(reservationService.reserve(shouldBePassed))
                .thenReturn(new ReservationComposeModel(
                        testCreator.newInstance(
                                newId,
                                new ReservationPeriod(req.fromDateTime(), req.toDateTime()),
                                req.note(),
                                new ItemId(req.itemId()),
                                reserverId),
                        item1,
                        user1));

        // when
        mockMvc.perform(post("/reserve/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newId.id()))
                .andExpect(jsonPath("$.fromDateTime").value(dateTimeFormatter.format(req.fromDateTime())))
                .andExpect(jsonPath("$.toDateTime").value(dateTimeFormatter.format(req.toDateTime())))
                .andExpect(jsonPath("$.note").value(req.note()))
                .andExpect(jsonPath("$.itemId").value(req.itemId()))
                .andExpect(jsonPath("$.serialNo").value(item1.getSerialNo()))
                .andExpect(jsonPath("$.itemName").value(item1.getItemName()))
                .andExpect(jsonPath("$.reserverId").value(reserverId.id()));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testReserveOnParameterError() throws Exception {

        // given
        ReserveItemRequest req = ReserveItemRequest.builder()
                .build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(post("/reserve/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("fromDateTime"),
                        containsString("toDateTime"),
                        containsString("itemId") //
                )));

        // then
        verify(reservationService, never()).reserve(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testReserveOnDuplicate() throws Exception {

        ReserveItemRequest req = createReserveItemRequest();
        String requestBody = mapper.writeValueAsString(req);

        ReserveItemCommand shouldBePassed = req.toCommand();
        when(reservationService.reserve(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        mockMvc.perform(post("/reserve/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
    }

    @Test
    public void testReserveOnAuthenticationError() throws Exception {

        // given
        ReserveItemRequest req = createReserveItemRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(post("/reserve/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).reserve(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testCancel() throws Exception {

        // given
        int reservationId = 1;
        doNothing().when(reservationService).cancel(new ReservationId(reservationId));

        // when
        mockMvc.perform(delete("/reserve/reservations/{id}", reservationId))
                // then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testCancelOnParameterError() throws Exception {

        // given
        int invalidId = -1;

        // when
        mockMvc.perform(delete("/reserve/reservations/{id}", invalidId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"), //
                        containsString("id"))));

        // then
        verify(reservationService, never()).cancel(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testCancelOnOthreUserReservation() throws Exception {

        // given
        int reservationId = 1;
        doThrow(new BusinessFlowException("from mock", CauseType.FORBIDDEN))
                .when(reservationService).cancel(new ReservationId(reservationId));

        // when
        mockMvc.perform(delete("/reserve/reservations/{id}", reservationId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("FORBIDDEN")));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void testDeleteOnNotFound() throws Exception {

        // given
        int reservationId = 999;
        doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(reservationService).cancel(new ReservationId(reservationId));

        // when
        mockMvc.perform(delete("/reserve/reservations/{id}", reservationId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
    }

    @Test
    public void testDeleteOnAuthenticationError() throws Exception {

        // given
        int reservationId = 1;

        // when
        mockMvc.perform(delete("/reserve/reservations/{id}", reservationId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(reservationService, never()).cancel(any());
    }

    private ReserveItemRequest createReserveItemRequest() {

        LocalDateTime from = LocalDateTime.now().plusHours(1);
        LocalDateTime to = from.plusHours(1);
        String note = "ReserveItem Note";
        ItemId itemId = new ItemId(1);

        return ReserveItemRequest.builder()
                .fromDateTime(from)
                .toDateTime(to)
                .note(note)
                .itemId(itemId.id())
                .build();
    }
}
