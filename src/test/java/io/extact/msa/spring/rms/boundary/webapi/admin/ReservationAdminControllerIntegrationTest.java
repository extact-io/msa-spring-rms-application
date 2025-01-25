package io.extact.msa.spring.rms.boundary.webapi.admin;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.web.RestClientBuilder;
import io.extact.msa.spring.rms.WebApiApplication;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class ReservationAdminControllerIntegrationTest {

    @Autowired
    private ReservationClient reservationClient;

    @Configuration(proxyBeanMethods = false)
    @Import(WebApiApplication.class)
    static class TestConfig {
        @Bean
        ReservationClient reservationClient(RestClientBuilder builder) {
            var factory = builder.build(RestClientAdapter.class);
            return factory.createClient(ReservationClient.class);
        }
    }

    @Test
    @Order(1)
    void testGetAll() {
        // when
        List<ReservationAdminResponse> reservations = reservationClient.getAll();

        // then
        assertThat(reservations).isNotEmpty();
        assertThat(reservations.get(0).id()).isEqualTo(1);
        assertThat(reservations.get(0).note()).isEqualTo("Reservation Note 1");
    }

    @Test
    void testGetAllReturnEmpty() {
        // Assume no reservations exist in the system.
        List<ReservationAdminResponse> reservations = reservationClient.getAll();

        assertThat(reservations).isEmpty();
    }

    @Test
    void testGetAllOnAuthenticationError() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(reservationClient::getAll)
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("認証エラー");
    }

    @Test
    @Order(2)
    void testUpdate() {
        // given
        ReservationUpdateRequest updateRequest = ReservationUpdateRequest.builder()
                .id(1)
                .fromDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
                .toDateTime(LocalDateTime.of(2025, 1, 1, 12, 0))
                .note("Updated Reservation Note")
                .build();

        // when
        ReservationAdminResponse updatedReservation = reservationClient.update(updateRequest);

        // then
        assertThat(updatedReservation.id()).isEqualTo(1);
        assertThat(updatedReservation.note()).isEqualTo("Updated Reservation Note");
    }

    @Test
    void testUpdateOnParameterError() {
        // given
        ReservationUpdateRequest invalidRequest = ReservationUpdateRequest.builder().build();

        // when / then
        assertThatThrownBy(() -> reservationClient.update(invalidRequest))
                .isInstanceOf(RmsValidationException.class)
                .hasMessageContaining("パラメーターエラーが発生しました");
    }

    @Test
    void testUpdateOnNotFound() {
        // given
        ReservationUpdateRequest nonExistentRequest = ReservationUpdateRequest.builder()
                .id(999)
                .note("Non-existent Reservation")
                .build();

        // when / then
        assertThatThrownBy(() -> reservationClient.update(nonExistentRequest))
                .isInstanceOf(BusinessFlowException.class)
                .hasMessageContaining("NOT_FOUND");
    }

    @Test
    void testUpdateOnDuplicate() {
        // given
        ReservationUpdateRequest duplicateRequest = ReservationUpdateRequest.builder()
                .id(1)
                .note("Duplicate Reservation")
                .build();

        // when / then
        assertThatThrownBy(() -> reservationClient.update(duplicateRequest))
                .isInstanceOf(BusinessFlowException.class)
                .hasMessageContaining("DUPLICATE");
    }

    @Test
    void testUpdateOnAuthenticationError() {
        SecurityContextHolder.clearContext();

        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .id(1)
                .note("Unauthorized Update")
                .build();

        assertThatThrownBy(() -> reservationClient.update(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("認証エラー");
    }

    @Test
    @Order(3)
    void testDelete() {
        // when
        reservationClient.delete(1);

        // then
        List<ReservationAdminResponse> reservations = reservationClient.getAll();
        assertThat(reservations).noneMatch(reservation -> reservation.id() == 1);
    }

    @Test
    void testDeleteOnParameterError() {
        assertThatThrownBy(() -> reservationClient.delete(-1))
                .isInstanceOf(RmsValidationException.class)
                .hasMessageContaining("パラメーターエラーが発生しました");
    }

    @Test
    void testDeleteOnNotFound() {
        assertThatThrownBy(() -> reservationClient.delete(999))
                .isInstanceOf(BusinessFlowException.class)
                .hasMessageContaining("NOT_FOUND");
    }

    @Test
    void testDeleteOnAuthenticationError() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> reservationClient.delete(1))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("認証エラー");
    }

    @HttpExchange("/reservations")
    public interface ReservationClient {
        @GetExchange
        List<ReservationAdminResponse> getAll();

        @PutExchange
        ReservationAdminResponse update(ReservationUpdateRequest request);

        @DeleteExchange("/{id}")
        void delete(int reservationId);
    }
}
