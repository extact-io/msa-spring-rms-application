package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static io.extact.msa.spring.rms.testutils.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.LocalDateTime;
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
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.feature.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.SecurityConstraintException;
import io.extact.msa.spring.platform.fw.infrastructure.external.converter.ConfigConversionServiceBuilder;
import io.extact.msa.spring.platform.fw.infrastructure.external.converter.ConfigMessageConveterBuilder;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationUpdateRequest.ReservationUpdateRequestBuilder;
import io.extact.msa.spring.rms.testutils.TestAuthUtils;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "file-all" })
@TestMethodOrder(OrderAnnotation.class)
class ReservationAdminControllerIntegrationTest {

    private static final ReservationAdminResponse reservation1 = ReservationAdminResponse.from(model1);
    private static final ReservationAdminResponse reservation2 = ReservationAdminResponse.from(model2);
    private static final ReservationAdminResponse reservation3 = ReservationAdminResponse.from(model3);

    @Autowired
    private ReservationClient client;

    @Configuration(proxyBeanMethods = false)
    @Import(WebApiApplication.class)
    static class TestConfig {
        @Bean
        ReservationClient reservationClient(Environment env) {

            HttpMessageConverter<Object> converter = ConfigMessageConveterBuilder
                    .builder(env)
                    .build();
            ConversionService conversionService = ConfigConversionServiceBuilder
                    .builder(env)
                    .build();

            RestClient restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
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
        TestAuthUtils.signinByJwt(generator, 1, "ADMIN");
    }

    @AfterEach
    void afterEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    @Order(1)
    void testGetAll() {
        // given
        List<ReservationAdminResponse> expected = List.of(reservation1, reservation2, reservation3);
        // when
        List<ReservationAdminResponse> actual = client.getAll();
        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testGetAllOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(client::getAll)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
        // when
        assertThatThrownBy(client::getAll)
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(2)
    void testUpdate() {
        // given
        ReservationUpdateRequest req = reservationUpdateRequestBuilder()
                .fromDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
                .toDateTime(LocalDateTime.of(2025, 1, 1, 12, 0))
                .note("Updated Reservation Note")
                .build();
        // when
        ReservationAdminResponse actual = client.update(req);
        // then
        assertThat(actual).isEqualTo(new ReservationAdminResponse(
                req.id(),
                req.fromDateTime(),
                req.toDateTime(),
                req.note(),
                reservation2.itemId(),
                reservation2.reserverId(),
                reservation2.item(),
                reservation2.reserver()));
    }

    @Test
    void testUpdateOnParameterError() {
        // given
        ReservationUpdateRequest invalidRequest = ReservationUpdateRequest.builder()
                .build(); // empty value
        // when
        assertThatThrownBy(() -> client.update(invalidRequest))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(3);
                    assertThat(thrown.getDetailMessage()).contains("id", "fromDateTime", "toDateTime");
                });
    }

    @Test
    void testUpdateOnNotFound() {
        // given
        ReservationUpdateRequest nonExistentRequest = reservationUpdateRequestBuilder()
                .id(999) // override
                .build();
        // when
        assertThatThrownBy(() -> client.update(nonExistentRequest))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    void testUpdateOnDuplicate() {
        // given
        ReservationUpdateRequest duplicateRequest = reservationUpdateRequestBuilder()
                .fromDateTime(reservation3.fromDateTime())
                .toDateTime(reservation3.toDateTime())
                .build();
        // when
        assertThatThrownBy(() -> client.update(duplicateRequest))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.DUPLICATE);
                });
    }

    @Test
    void testUpdateOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        ReservationUpdateRequest request = reservationUpdateRequestBuilder().build();
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @Test
    @Order(3)
    void testDelete() {
        // given
        int deleteId = 1;
        // when
        client.delete(1);
        // then
        ReservationAdminResponse deleted = client.getAll().stream()
                .filter(r -> r.id() == deleteId)
                .findFirst()
                .orElse(null);
        assertThat(deleted).isNull();
    }

    @Test
    void testDeleteOnParameterError() {
        // given
        int errorId = -1;
        // when
        assertThatThrownBy(() -> client.delete(errorId))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().messageItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("reservationId");
                });
    }

    @Test
    void testDeleteOnNotFound() {
        // given
        int notExistId = 999;
        // when
        assertThatThrownBy(() -> client.delete(notExistId))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    void testDeleteOnAuthError(@Autowired JsonWebTokenGenerator generator) {
        // given -- 認証エラー
        SecurityContextHolder.clearContext();
        int deleteId = 1;
        // when
        assertThatThrownBy(() -> client.delete(deleteId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });

        // given -- 認可エラー
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
        // when
        assertThatThrownBy(() -> client.delete(deleteId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認可エラー");
                });
    }

    @HttpExchange("/admin/reservations")
    public interface ReservationClient {
        @GetExchange
        List<ReservationAdminResponse> getAll();

        @PutExchange
        ReservationAdminResponse update(@RequestBody ReservationUpdateRequest request);

        @DeleteExchange("/{id}")
        void delete(@PathVariable("id") int reservationId);
    }

    private ReservationUpdateRequestBuilder reservationUpdateRequestBuilder() {
        return ReservationUpdateRequest.builder()
                .id(reservation2.id())
                .fromDateTime(reservation2.fromDateTime())
                .toDateTime(reservation2.toDateTime())
                .note(reservation2.note());
    }
}
