package io.extact.msa.spring.rms.interfaces.webapi.member;

import static io.extact.msa.spring.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.extact.msa.spring.PersistedTestData;
import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.core.jwt.encode.JwtEncodeConfig;
import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.SecurityConstraintException;
import io.extact.msa.spring.platform.test.stub.auth.TestAuthUtils;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationAdminResponse;
import io.extact.msa.spring.rms.interfaces.webapi.member.ReserveItemRequest.ReserveItemRequestBuilder;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "file-all", "test" })
@TestMethodOrder(OrderAnnotation.class)
class ItemReservationControllerIntegrationTest {

    private static final int NO_SIDE_EFFECT_CASE = 1;
    private static final int WITH_SIDE_EFFECT_CASE = 99;

    private static final ReserveItemResponse reservation1 = ReserveItemResponse.from(model1);
    private static final ReserveItemResponse reservation2 = ReserveItemResponse.from(model2);
    private static final ReserveItemResponse reservation3 = ReserveItemResponse.from(model3);

    private static final ItemResponse item1 = ItemResponse.from(PersistedTestData.item1);
    private static final ItemResponse item2 = ItemResponse.from(PersistedTestData.item2);
    private static final ItemResponse item3 = ItemResponse.from(PersistedTestData.item3);
    private static final ItemResponse item4 = ItemResponse.from(PersistedTestData.item4);

    @Autowired
    private ReservationClient client;
    private int defaultLoginUserId = 1;

    @Configuration(proxyBeanMethods = false)
    @Import({
        WebApiApplication.class,
        JwtEncodeConfig.class
    })
    static class TestConfig {
        @Bean
        ReservationClient reservationClient(Environment env) {
            RestClient restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();

            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(ReservationClient.class);
        }
    }

    @BeforeEach
    void beforeEach(@Autowired JsonWebTokenGenerator generator) {
        TestAuthUtils.signinByJwt(generator, defaultLoginUserId, "MEMBER");
    }

    @AfterEach
    void afterEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testGetItemAll() {
        // given
        List<ItemResponse> expected = List.of(item1, item2, item3, item4);
        // when
        List<ItemResponse> actual = client.getItemAll();
        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testGetItemAllOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(client::getItemAll)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindRentableItemAtPeriod() {

        // given -- found
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        List<ItemResponse> actual = client.findRentableItemAtPeriod(from, to);
        // then
        assertThat(actual).containsExactly(item1, item2, item4);
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindRentableItemAtPeriodOnParameterError() {
        // given
        LocalDateTime from = null;
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        assertThatThrownBy(() -> client.findRentableItemAtPeriod(from, to))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("from");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindRentableItemAtPeriodOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        assertThatThrownBy(() -> client.findRentableItemAtPeriod(from, to))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testIsRentableItemAtPeriod() {

        // given -- OK
        int itemId = 1;
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        boolean actual = client.isRentableItemAtPeriod(itemId, from, to);
        // then
        assertThat(actual).isTrue();

        // given -- NG
        itemId = 3;
        from = LocalDateTime.of(2020, 4, 1, 9, 0);
        to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        actual = client.isRentableItemAtPeriod(itemId, from, to);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testIsRentableItemAtPeriodOnPathVariableError() {
        // given
        int itemId = -1;
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        assertThatThrownBy(() -> client.isRentableItemAtPeriod(itemId, from, to))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("itemId");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testIsRentableItemAtPeriodOnRequestPrameterError() {
        // given
        int itemId = 1;
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 9, 0);
        LocalDateTime to = null;
        // when
        assertThatThrownBy(() -> client.isRentableItemAtPeriod(itemId, from, to))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("to");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testIsRentableItemAtPeriodOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        int itemId = 1;
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        assertThatThrownBy(() -> client.isRentableItemAtPeriod(itemId, from, to))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByItemId() {

        // given -- found
        int itemId = 3;
        LocalDate fromDate = null;
        // when
        List<ReserveItemResponse> actual = client.findReservationByItemId(itemId, fromDate);
        // then
        assertThat(actual).containsExactly(reservation1, reservation2, reservation3);

        // given -- not found
        itemId = 1;
        fromDate = null;
        // when
        actual = client.findReservationByItemId(itemId, fromDate);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByItemIdWithFromDate() {

        // given -- found
        int itemId = 3;
        LocalDate fromDate = LocalDate.of(2020, 4, 1);
        // when
        List<ReserveItemResponse> actual = client.findReservationByItemId(itemId, fromDate);
        // then
        assertThat(actual).containsExactly(reservation1, reservation2);

        // given -- not found
        itemId = 3;
        fromDate = LocalDate.of(2020, 4, 2);
        // when
        actual = client.findReservationByItemId(itemId, fromDate);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByItemIdOnParameterError() {

        // given -- on PathVariable error
        int invalidId = -1;
        LocalDate date = null;
        // when
        assertThatThrownBy(() -> client.findReservationByItemId(invalidId, date))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("itemId");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByItemIdOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        int itemId = 3;
        LocalDate fromDate = LocalDate.of(2020, 4, 1);
        // when
        assertThatThrownBy(() -> client.findReservationByItemId(itemId, fromDate))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByReserverId() {

        // given -- found
        int reserverId = 1;
        // when
        List<ReserveItemResponse> actual = client.findReservationByReserverId(reserverId);
        // then
        assertThat(actual).containsExactly(reservation1, reservation3);

        // given -- not found
        reserverId = 3;
        // when
        actual = client.findReservationByReserverId(reserverId);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByReserverIdOnParameterError() {
        // given
        int invalidId = -1;
        // when
        assertThatThrownBy(() -> client.findReservationByReserverId(invalidId))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("reserverId");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByReserverIdOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        int reserverId = 1;
        // when
        assertThatThrownBy(() -> client.findReservationByReserverId(reserverId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testGetOwnReservations() {
        // given
        // -- userId(1) LoggedIn.
        // when
        List<ReserveItemResponse> actual = client.getOwnReservations();
        // then
        assertThat(actual).containsExactly(reservation1, reservation3);
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testGetOwnReservationsOnReturnEmpty(@Autowired JsonWebTokenGenerator generator) {
        // given
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        List<ReserveItemResponse> actual = client.getOwnReservations();
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testGetOwnReservationsOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(() -> client.getOwnReservations())
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testReserve() {
        // given
        ReserveItemRequest request = reserveItemRequestBuilder().build();
        // when
        ReserveItemResponse actual = client.reserve(request);
        // then
        assertThat(actual).isEqualTo(new ReserveItemResponse(
                4,
                request.fromDateTime(),
                request.toDateTime(),
                request.note(),
                request.itemId(),
                item3.serialNo(),
                item3.itemName(),
                defaultLoginUserId));
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testReserveOnParameterError() {
        // given
        ReserveItemRequest request = ReserveItemRequest.builder()
                .build(); // empty value
        // when
        assertThatThrownBy(() -> client.reserve(request))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(3);
                    assertThat(thrown.getDetailMessage()).contains(
                            "fromDateTime",
                            "toDateTime",
                            "itemId");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testReserveOnParameterErrorByFutureDate() {
        // given -- overrapped period
        ReserveItemRequest duplicateRequest = reserveItemRequestBuilder()
                .fromDateTime(LocalDateTime.of(2020, 4, 1, 9, 0)) // 過去日なのでエラー
                .toDateTime(LocalDateTime.of(2020, 4, 1, 12, 0))
                .itemId(3)
                .build();
        // when
        assertThatThrownBy(() -> client.reserve(duplicateRequest))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("fromDateTime");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testReserveOnParameterErrorByBeforeAfter() {
        // given
        ReserveItemRequest duplicateRequest = reserveItemRequestBuilder()
                .fromDateTime(LocalDateTime.now().plusHours(2)) // 開始と終了が逆
                .toDateTime(LocalDateTime.now().plusHours(1))
                .itemId(3)
                .build();
        // when
        assertThatThrownBy(() -> client.reserve(duplicateRequest))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("開始日", "終了日");
                });
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE + 1)
    void testReserveOnDuplicate() {

        // given
        ReserveItemRequest preconditionRequest = reserveItemRequestBuilder()
                .fromDateTime(LocalDateTime.now().plusHours(1))
                .toDateTime(LocalDateTime.now().plusHours(2))
                .itemId(1)
                .build();
        ReserveItemResponse precondition = client.reserve(preconditionRequest);

        ReserveItemRequest duplicateRequest = reserveItemRequestBuilder()
                .fromDateTime(precondition.fromDateTime())
                .toDateTime(precondition.toDateTime())
                .itemId(precondition.itemId())
                .build();
        // when
        assertThatThrownBy(() -> client.reserve(duplicateRequest))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.DUPLICATE);
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testReserveOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        ReserveItemRequest request = reserveItemRequestBuilder().build();
        // when
        assertThatThrownBy(() -> client.reserve(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testCancel() {
        // given
        int cancelId = 1;
        // when
        client.cancel(cancelId);
        // then
        ReservationAdminResponse deleted = client.getReservationAllForAssertUse().stream()
                .filter(r -> r.id() == cancelId)
                .findFirst()
                .orElse(null);
        assertThat(deleted).isNull();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testCancelOnParameterError() {
        // given
        int errorId = -1;
        // when
        assertThatThrownBy(() -> client.cancel(errorId))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("reservationId");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testCancelOnOthreUserReservation(@Autowired JsonWebTokenGenerator generator) {
        // given
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        int cancelId = 1;
        // when
        assertThatThrownBy(() -> client.cancel(cancelId))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.FORBIDDEN);
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testDeleteOnNotFound() {
        // given
        int notExistId = 999;
        // when
        assertThatThrownBy(() -> client.cancel(notExistId))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testDeleteOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        int cancelId = 1;
        // when
        assertThatThrownBy(() -> client.cancel(cancelId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @HttpExchange
    public interface ReservationClient {

        @GetExchange("/member/items")
        List<ItemResponse> getItemAll();

        @GetExchange("/member/items/rentable")
        List<ItemResponse> findRentableItemAtPeriod(
                @RequestParam("from") LocalDateTime from,
                @RequestParam("to") LocalDateTime to);

        @GetExchange("/member/items/{itemId}/rentable")
        boolean isRentableItemAtPeriod(
                @PathVariable("itemId") @RmsId Integer itemId,
                @RequestParam("from") LocalDateTime from,
                @RequestParam("to") LocalDateTime to);

        @GetExchange("/member/reservations/items/{itemId}")
        List<ReserveItemResponse> findReservationByItemId(
                @PathVariable("itemId") Integer itemId,
                @RequestParam(value = "from-date", required = false) LocalDate from);

        @GetExchange("/member/reservations/reservers/{reserverId}")
        List<ReserveItemResponse> findReservationByReserverId(
                @PathVariable("reserverId") Integer reserverId);

        @GetExchange("/member/reservations/own")
        List<ReserveItemResponse> getOwnReservations();

        @PostExchange("/member/reservations")
        ReserveItemResponse reserve(@RequestBody ReserveItemRequest request);

        @DeleteExchange("/member/reservations/{reservationId}")
        void cancel(@PathVariable("reservationId") Integer reservationId);

        @GetExchange("/admin/reservations") // for assert use only
        List<ReservationAdminResponse> getReservationAllForAssertUse();
    }

    private ReserveItemRequestBuilder reserveItemRequestBuilder() {
        return ReserveItemRequest.builder()
                .fromDateTime(LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                .toDateTime(LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES))
                .note("New Reservation note")
                .itemId(3);
    }
}
