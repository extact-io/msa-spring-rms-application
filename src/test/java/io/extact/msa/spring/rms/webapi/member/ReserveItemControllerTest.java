package io.extact.msa.spring.rms.webapi.member;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import io.extact.msa.spring.platform.fw.feature.exception.RmsRequestCheckException;
import io.extact.msa.spring.rms.application.member.ReserveItemCommand;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import io.extact.msa.spring.rms.application.member.ReserveItemService;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.webapi.WebApiConfig.WebApiContextConfigs;

/**
 * Controller 単体テスト。
 * ControllerAdvice / Spring Security / Method Validation を有効にしている。
 * Spring Boot 3.4 で追加された AssertJ 向け MockMvcTester を使用。
 */
@WebMvcTest
@ActiveProfiles("test")
class ReserveItemControllerTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {
    };
    private static DateTimeFormatter dateTimeFormatter;

    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private ReserveItemService reservationService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            WebApiContextConfigs.class
    })
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

    // ---------------------------------------------------------------------
    // item
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testGetItemAll() throws Exception {

        // given
        when(reservationService.getItemAll())
                .thenReturn(List.of(item1, item2, item3, item4));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(4))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(item1.getId().id()))
                .hasPathSatisfying("$[0].serialNo", p -> p.assertThat().isEqualTo(item1.getSerialNo()))
                .hasPathSatisfying("$[0].itemName", p -> p.assertThat().isEqualTo(item1.getItemName())); // 2件目以降は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testGetItemAllReturnEmpty() throws Exception {

        // given
        when(reservationService.getItemAll())
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).getItemAll();
    }

    @Test
    void testGetItemAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser なし

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).getItemAll();
    }

    // ---------------------------------------------------------------------
    // rentable items
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindRentableItemAtPeriod() throws Exception {

        // given
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.findRentableItemAtPeriod(from, to))
                .thenReturn(List.of(item2, item4));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/rentable")
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(2))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(item2.getId().id()))
                .hasPathSatisfying("$[0].serialNo", p -> p.assertThat().isEqualTo(item2.getSerialNo()))
                .hasPathSatisfying("$[0].itemName", p -> p.assertThat().isEqualTo(item2.getItemName())); // 2件目以降は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindRentableItemAtPeriodReturnEmpty() throws Exception {

        // given
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        when(reservationService.findRentableItemAtPeriod(from, to))
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/rentable")
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));

        // then -- mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findRentableItemAtPeriod(from, to);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindRentableItemAtPeriodOnParameterError() throws Exception {

        // given
        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/rentable")
                .queryParam("from", "") // パラメータ不足
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "from");

        // then
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }

    @Test
    void testFindRentableItemAtPeriodOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/rentable")
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).findRentableItemAtPeriod(any(), any());
    }

    // ---------------------------------------------------------------------
    // isRentable
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testIsRentableItemAtPeriod() throws Exception {

        // given
        ItemId itemId = new ItemId(1);
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);
        boolean returnValue = false;
        when(reservationService.isRentableItemAtPeriod(itemId, from, to))
                .thenReturn(returnValue);

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/{itemId}/rentable", itemId.id())
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyText()
                .isEqualTo(String.valueOf(returnValue));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testIsRentableItemAtPeriodOnPathVariableError() throws Exception {

        // given
        String itemId = "a"; // コンバートエラー
        String from = ""; // リクエストパラメータなし
        String to = LocalDateTime.of(2025, 1, 1, 12, 0).toString();

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/{itemId}/rentable", itemId)
                .queryParam("from", from)
                .queryParam("to", to)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "itemId");
        verify(reservationService, never()).isRentableItemAtPeriod(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testIsRentableItemAtPeriodOnRequestParameterError() throws Exception {

        // given
        String itemId = "1";
        String from = ""; // リクエストパラメータなし
        String to = LocalDateTime.of(2025, 1, 1, 12, 0).toString();

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/{itemId}/rentable", itemId)
                .queryParam("from", from)
                .queryParam("to", to)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "from");
        verify(reservationService, never()).isRentableItemAtPeriod(any(), any(), any());
    }

    @Test
    void testIsRentableItemAtPeriodOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        ItemId itemId = new ItemId(1);
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 12, 0);

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/items/{itemId}/rentable", itemId.id())
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).isRentableItemAtPeriod(any(), any(), any());
    }

    // ---------------------------------------------------------------------
    // findReservationByCondition(with itemId)
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByItemId() throws Exception {

        // given
        Integer itemId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of(model1, model2));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("item-id", itemId.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(2))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(model1.reservation().getId().id()))
                .hasPathSatisfying("$[0].fromDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .hasPathSatisfying("$[0].toDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .hasPathSatisfying("$[0].note", p -> p.assertThat().isEqualTo(model1.reservation().getNote()))
                .hasPathSatisfying("$[0].itemId", p -> p.assertThat().isEqualTo(model1.reservation().getItemId().id()))
                .hasPathSatisfying("$[0].serialNo", p -> p.assertThat().isEqualTo(model1.rentalItem().getSerialNo()))
                .hasPathSatisfying("$[0].itemName", p -> p.assertThat().isEqualTo(model1.rentalItem().getItemName()))
                .hasPathSatisfying("$[0].reserverId",
                        p -> p.assertThat().isEqualTo(model1.reservation().getReserverId().id())); // 2件目以降は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByItemIdWithFromDate() throws Exception {

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
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("item-id", itemId.toString())
                .queryParam("from-date", fromDate.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(2))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(model1.reservation().getId().id()))
                .hasPathSatisfying("$[0].fromDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .hasPathSatisfying("$[0].toDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .hasPathSatisfying("$[0].note", p -> p.assertThat().isEqualTo(model1.reservation().getNote()))
                .hasPathSatisfying("$[0].itemId", p -> p.assertThat().isEqualTo(model1.reservation().getItemId().id()))
                .hasPathSatisfying("$[0].serialNo", p -> p.assertThat().isEqualTo(model1.rentalItem().getSerialNo()))
                .hasPathSatisfying("$[0].itemName", p -> p.assertThat().isEqualTo(model1.rentalItem().getItemName()))
                .hasPathSatisfying("$[0].reserverId",
                        p -> p.assertThat().isEqualTo(model1.reservation().getReserverId().id())); // 2件目以降は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByItemIdWithFromDateNull() throws Exception {

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
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("item-id", itemId.toString())
                .queryParam("from-date", "")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(2)); // 以降省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByItemIdReturnEmpty() throws Exception {

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
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("item-id", itemId.toString())
                .queryParam("from-date", fromDate.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));
        // mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByItemIdOnParameterError() throws Exception {

        // given
        Integer invalidItemId = -1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(invalidItemId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("item-id", invalidItemId.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));
        // mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    void testFindReservationByItemIdOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        Integer itemId = -1;

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("item-id", itemId.toString())
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).findReservationByCondition(any());
    }

    // ---------------------------------------------------------------------
    // findReservationByCondition(with resereverId)
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByReserverId() throws Exception {

        // given
        Integer reserverId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(reserverId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of(model1, model2));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("reserver-id", reserverId.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(2))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(model1.reservation().getId().id()))
                .hasPathSatisfying("$[0].fromDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .hasPathSatisfying("$[0].toDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .hasPathSatisfying("$[0].note", p -> p.assertThat().isEqualTo(model1.reservation().getNote()))
                .hasPathSatisfying("$[0].itemId", p -> p.assertThat().isEqualTo(model1.reservation().getItemId().id()))
                .hasPathSatisfying("$[0].serialNo", p -> p.assertThat().isEqualTo(model1.rentalItem().getSerialNo()))
                .hasPathSatisfying("$[0].itemName", p -> p.assertThat().isEqualTo(model1.rentalItem().getItemName()))
                .hasPathSatisfying("$[0].reserverId",
                        p -> p.assertThat().isEqualTo(model1.reservation().getReserverId().id())); // 2件目以降は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByReserverIdReturnEmpty() throws Exception {

        // given
        Integer reserverId = 1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(reserverId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("reserver-id", reserverId.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));
        // mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationByReserverIdOnParameterError() throws Exception {

        // given
        Integer invalidReserverId = -1;
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(invalidReserverId)
                .build();
        when(reservationService.findReservationByCondition(cond))
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("reserver-id", invalidReserverId.toString())
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));
        // mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).findReservationByCondition(cond);
    }

    @Test
    void testFindReservationByReserverIdOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "MEMBER")なし
        Integer reserverId = -1;

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .queryParam("reserver-id", reserverId.toString())
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).findReservationByCondition(any());
    }

    // ---------------------------------------------------------------------
    // findReservationByCondition(parameter error)
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testFindReservationOnNoParameterError() throws Exception {

        // given
        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader("rms-exception", RmsRequestCheckException.class.getSimpleName());
        verify(reservationService, never()).findReservationByCondition(any());
    }

    // ---------------------------------------------------------------------
    // getOwnReservations
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testGetOwnReservations() throws Exception {

        // given
        when(reservationService.getOwnReservations())
                .thenReturn(List.of(model1, model2));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations/own")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(2))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(model1.reservation().getId().id()))
                .hasPathSatisfying("$[0].fromDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .hasPathSatisfying("$[0].toDateTime",
                        p -> p.assertThat()
                                .isEqualTo(dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .hasPathSatisfying("$[0].note", p -> p.assertThat().isEqualTo(model1.reservation().getNote()))
                .hasPathSatisfying("$[0].itemId", p -> p.assertThat().isEqualTo(model1.reservation().getItemId().id()))
                .hasPathSatisfying("$[0].serialNo", p -> p.assertThat().isEqualTo(model1.rentalItem().getSerialNo()))
                .hasPathSatisfying("$[0].itemName", p -> p.assertThat().isEqualTo(model1.rentalItem().getItemName()))
                .hasPathSatisfying("$[0].reserverId",
                        p -> p.assertThat().isEqualTo(model1.reservation().getReserverId().id())); // 2件目以降は省略
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testGetOwnReservationsReturnEmpty() throws Exception {

        // given
        when(reservationService.getOwnReservations())
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations/own")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));

        // mockのデフォルトの空リストと判別がつくように呼ばれていることを検証する
        verify(reservationService, only()).getOwnReservations();
    }

    @Test
    void testGetOwnReservationsOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser なし

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/member/reservations/own")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).getOwnReservations();
    }

    // ---------------------------------------------------------------------
    // reserve
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testReserve() throws Exception {

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
        MvcTestResult result = mockMvc
                .post()
                .uri("/member/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(newId.id()))
                .hasPathSatisfying("$.fromDateTime",
                        p -> p.assertThat().isEqualTo(dateTimeFormatter.format(req.fromDateTime())))
                .hasPathSatisfying("$.toDateTime",
                        p -> p.assertThat().isEqualTo(dateTimeFormatter.format(req.toDateTime())))
                .hasPathSatisfying("$.note", p -> p.assertThat().isEqualTo(req.note()))
                .hasPathSatisfying("$.itemId", p -> p.assertThat().isEqualTo(req.itemId()))
                .hasPathSatisfying("$.serialNo", p -> p.assertThat().isEqualTo(item1.getSerialNo()))
                .hasPathSatisfying("$.itemName", p -> p.assertThat().isEqualTo(item1.getItemName()))
                .hasPathSatisfying("$.reserverId", p -> p.assertThat().isEqualTo(reserverId.id()));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testReserveOnParameterError() throws Exception {

        // given
        ReserveItemRequest req = ReserveItemRequest.builder().build(); // empty value
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/member/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました",
                        "fromDateTime", "toDateTime", "itemId");
        verify(reservationService, never()).reserve(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testReserveOnDuplicate() throws Exception {

        // given
        ReserveItemRequest req = createReserveItemRequest();
        String requestBody = mapper.writeValueAsString(req);

        ReserveItemCommand shouldBePassed = req.toCommand();
        when(reservationService.reserve(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/member/reservations")
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
    void testReserveOnAuthenticationError() throws Exception {

        // given
        ReserveItemRequest req = createReserveItemRequest();
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/member/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).reserve(any());
    }

    // ---------------------------------------------------------------------
    // cancel
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testCancel() throws Exception {

        // given
        int reservationId = 1;
        doNothing().when(reservationService).cancel(new ReservationId(reservationId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/member/reservations/{id}", reservationId)
                .exchange();

        // then
        assertThat(result).hasStatusOk();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testCancelOnParameterError() throws Exception {

        // given
        int invalidId = -1;

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/member/reservations/{id}", invalidId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "id");
        verify(reservationService, never()).cancel(any());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void testCancelOnOtherUserReservation() throws Exception {

        // given
        int reservationId = 1;
        doThrow(new BusinessFlowException("from mock", CauseType.FORBIDDEN))
                .when(reservationService).cancel(new ReservationId(reservationId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/member/reservations/{id}", reservationId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyText()
                .contains("FORBIDDEN");
    }

    // ---------------------------------------------------------------------
    // reserve
    // ---------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "MEMBER")
    void testDeleteOnNotFound() throws Exception {

        // given
        int reservationId = 999;
        doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(reservationService).cancel(new ReservationId(reservationId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/member/reservations/{id}", reservationId)
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
        int reservationId = 1;

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/member/reservations/{id}", reservationId)
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).cancel(any());
    }

    // -------------------------------------------------------- private methods

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
