package io.extact.msa.spring.rms.webapi.member;

import static io.extact.msa.spring.rms.PersistedTestData.*;
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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.converter.HttpMessageConverter;
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

import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.feature.exception.RmsRequestCheckException;
import io.extact.msa.spring.platform.fw.feature.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.SecurityConstraintException;
import io.extact.msa.spring.platform.fw.infrastructure.external.converter.ConfigConversionServiceBuilder;
import io.extact.msa.spring.platform.fw.infrastructure.external.converter.ConfigMessageConveterBuilder;
import io.extact.msa.spring.platform.fw.test.utils.TestAuthUtils;
import io.extact.msa.spring.rms.PersistedTestData;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.rms.webapi.admin.ReservationAdminResponse;
import io.extact.msa.spring.rms.webapi.member.ReserveItemRequest.ReserveItemRequestBuilder;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "file-all" })
@TestMethodOrder(OrderAnnotation.class)
class ReserveItemControllerIntegrationTest {

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
    @Import(WebApiApplication.class)
    static class TestConfig {

        @Bean
        @ConfigurationProperties("rms.persistence.reservation.remote")
        ExternalProperties externalProperties() {
            return new ExternalProperties();
        }

        @Bean
        ReservationClient reservationClient(ExternalProperties prop, ApplicationContext context) {

            HttpMessageConverter<Object> converter = ConfigMessageConveterBuilder
                    .builder(prop)
                    .build(context);
            ConversionService conversionService = ConfigConversionServiceBuilder
                    .builder(prop)
                    .build();

            RestClient restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(context.getEnvironment()))
                    .messageConverters(converters -> converters.addFirst(converter))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();

            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory
                    .builderFor(adapter)
                    .conversionService(conversionService)
                    .build();
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
    void testGetItemAllOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(client::getItemAll)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(client::getItemAll)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
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
                // サーバにリクエスト送信される前にClient側でエラーになる
                .isInstanceOfSatisfying(IllegalArgumentException.class, thrown -> {
                    assertThat(thrown.getMessage()).contains("from");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindRentableItemAtPeriodOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        LocalDateTime from = LocalDateTime.of(2020, 4, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2020, 4, 1, 11, 0);
        // when
        assertThatThrownBy(() -> client.findRentableItemAtPeriod(from, to))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(() -> client.findRentableItemAtPeriod(from, to))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
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
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(1);
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
                // サーバにリクエスト送信される前にClient側でエラーになる
                .isInstanceOfSatisfying(IllegalArgumentException.class, thrown -> {
                    assertThat(thrown.getMessage()).contains("to");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testIsRentableItemAtPeriodOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
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

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(() -> client.isRentableItemAtPeriod(itemId, from, to))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByItemId() {

        // given -- found
        Integer itemId = 3;
        Integer reserverId = null;
        LocalDate fromDate = null;
        // when
        List<ReserveItemResponse> actual = client.findReservationByParams(itemId, reserverId, fromDate);
        // then
        assertThat(actual).containsExactly(reservation1, reservation2, reservation3);

        // given -- not found
        itemId = 1;
        reserverId = null;
        fromDate = null;
        // when
        actual = client.findReservationByParams(itemId, reserverId, fromDate);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByItemIdWithFromDate() {

        // given -- found
        Integer itemId = 3;
        Integer reserverId = null;
        LocalDate fromDate = LocalDate.of(2020, 4, 1);
        // when
        List<ReserveItemResponse> actual = client.findReservationByParams(itemId, reserverId, fromDate);
        // then
        assertThat(actual).containsExactly(reservation1, reservation2);

        // given -- not found
        itemId = 3;
        reserverId = null;
        fromDate = LocalDate.of(2020, 4, 2);
        // when
        actual = client.findReservationByParams(itemId, reserverId, fromDate);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByInvalidItemId() {

        // given
        Integer invalidItemId = -1;
        LocalDate date = null;
        Integer reserverId = null;

        // when
        // 検索条件なので-1が来ても検索結果がないだけなのでエラーにはしない
        List<ReserveItemResponse> actual = client.findReservationByParams(invalidItemId, reserverId, date);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByItemIdOnAuthError(@Autowired JsonWebTokenGenerator generator) {

        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        Integer itemId = 3;
        LocalDate fromDate = LocalDate.of(2020, 4, 1);
        Integer reserverId = null;

        // when
        assertThatThrownBy(() -> client.findReservationByParams(itemId, reserverId, fromDate))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(() -> client.findReservationByParams(itemId, reserverId, fromDate))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByReserverId() {

        // given -- found
        Integer itemId = null;
        Integer reserverId = 1;
        LocalDate fromDate = null;
        // when
        List<ReserveItemResponse> actual = client.findReservationByParams(itemId, reserverId, fromDate);
        // then
        assertThat(actual).containsExactly(reservation1, reservation3);

        // given -- not found
        itemId = null;
        reserverId = 3;
        fromDate = null;
        // when
        actual = client.findReservationByParams(itemId, reserverId, fromDate);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByInvalidReserverId() {
        // given
        Integer itemId = null;
        Integer reserverId = -1;
        LocalDate fromDate = null;
        // when
        // 検索条件なので-1が来ても検索結果がないだけなのでエラーにはしない
        List<ReserveItemResponse> actual = client.findReservationByParams(itemId, reserverId, fromDate);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationByReserverIdOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        Integer itemId = null;
        Integer reserverId = 1;
        LocalDate fromDate = null;
        // when
        assertThatThrownBy(() -> client.findReservationByParams(itemId, reserverId, fromDate))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(() -> client.findReservationByParams(itemId, reserverId, fromDate))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testFindReservationOnNoParameterError() {
        // given
        // when
        assertThatThrownBy(() -> client.findReservationByParams(null, null, null))
                // then
                .isInstanceOfSatisfying(RmsRequestCheckException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("at least one request parameter");
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
        TestAuthUtils.signinByJwt(generator, 3, "MEMBER");
        // when
        List<ReserveItemResponse> actual = client.getOwnReservations();
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testGetOwnReservationsOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(() -> client.getOwnReservations())
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(() -> client.getOwnReservations())
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
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
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(3);
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
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(1);
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
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(1);
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
    void testReserveOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        ReserveItemRequest request = reserveItemRequestBuilder().build();
        // when
        assertThatThrownBy(() -> client.reserve(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(() -> client.reserve(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testCancel(@Autowired JsonWebTokenGenerator generator) {

        // given
        int cancelId = 1;
        // when
        client.cancel(cancelId);

        // then
        // getReservationAllForAssertUseはADMIN機能を使っているのでロールを切り替える
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
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
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("reservationId");
                });
    }

    @Test
    @Order(NO_SIDE_EFFECT_CASE)
    void testCancelOnOthreUserReservation(@Autowired JsonWebTokenGenerator generator) {
        // given
        TestAuthUtils.signoutQuietly();
        TestAuthUtils.signinByJwt(generator, 3, "MEMBER");
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
    void testCancelOnNotFound() {
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
    void testCancelOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        int cancelId = 1;
        // when
        assertThatThrownBy(() -> client.cancel(cancelId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 3, "ADMIN");
        // when
        assertThatThrownBy(() -> client.cancel(cancelId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @HttpExchange
    public interface ReservationClient {

        @GetExchange("/member/items")
        List<ItemResponse> getItemAll();

        @GetExchange("/member/items/rentable")
        List<ItemResponse> findRentableItemAtPeriod(
                @RequestParam LocalDateTime from,
                @RequestParam LocalDateTime to);

        @GetExchange("/member/items/{itemId}/rentable")
        boolean isRentableItemAtPeriod(
                @PathVariable @RmsId Integer itemId,
                @RequestParam LocalDateTime from,
                @RequestParam LocalDateTime to);

        @GetExchange("/member/reservations")
        public List<ReserveItemResponse> findReservationByParams(
                @RequestParam(name = "item-id", required = false) Integer itemId,
                @RequestParam(name = "reserver-id", required = false) Integer reserverId,
                @RequestParam(name = "from-date", required = false) LocalDate from);

        @GetExchange("/member/reservations/own")
        List<ReserveItemResponse> getOwnReservations();

        @PostExchange("/member/reservations")
        ReserveItemResponse reserve(@RequestBody ReserveItemRequest request);

        @DeleteExchange("/member/reservations/{reservationId}")
        void cancel(@PathVariable Integer reservationId);

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
