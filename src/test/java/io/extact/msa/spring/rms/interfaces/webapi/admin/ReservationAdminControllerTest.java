package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationUpdateCommand;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.interfaces.webapi.WebApiConfig.WebApiContextConfigs;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationUpdateRequest.ReservationUpdateRequestBuilder;

@WebMvcTest
@ActiveProfiles("test")
class ReservationAdminControllerTest {

    private static final ReservationCreatable testCreator = new ReservationCreatable() {
    };
    private static DateTimeFormatter dateTimeFormatter;

    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private ReservationAdminService reservationService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            WebApiContextConfigs.class
    })
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
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/reservations")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(3))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(model1.reservation().getId().id()))
                .hasPathSatisfying("$[0].fromDateTime", p -> p.assertThat().isEqualTo(
                        dateTimeFormatter.format(model1.reservation().getPeriod().getFrom())))
                .hasPathSatisfying("$[0].toDateTime", p -> p.assertThat().isEqualTo(
                        dateTimeFormatter.format(model1.reservation().getPeriod().getTo())))
                .hasPathSatisfying("$[0].note", p -> p.assertThat().isEqualTo(model1.reservation().getNote()))
                .hasPathSatisfying("$[0].itemId", p -> p.assertThat().isEqualTo(model1.reservation().getItemId().id()))
                .hasPathSatisfying("$[0].reserverId",
                        p -> p.assertThat().isEqualTo(model1.reservation().getReserverId().id()))
                .hasPathSatisfying("$[0].item.id", p -> p.assertThat().isEqualTo(model1.rentalItem().getId().id()))
                .hasPathSatisfying("$[0].item.serialNo",
                        p -> p.assertThat().isEqualTo(model1.rentalItem().getSerialNo()))
                .hasPathSatisfying("$[0].item.itemName",
                        p -> p.assertThat().isEqualTo(model1.rentalItem().getItemName()))
                .hasPathSatisfying("$[0].reserver.id", p -> p.assertThat().isEqualTo(model1.reserver().getId().id()))
                .hasPathSatisfying("$[0].reserver.loginId",
                        p -> p.assertThat().isEqualTo(model1.reserver().getLoginId()))
                .hasPathSatisfying("$[0].reserver.password",
                        p -> p.assertThat().isEqualTo(model1.reserver().getPassword()))
                .hasPathSatisfying("$[0].reserver.userType",
                        p -> p.assertThat().isEqualTo(model1.reserver().getUserType().name()))
                .hasPathSatisfying("$[0].reserver.userName",
                        p -> p.assertThat().isEqualTo(model1.reserver().getProfile().getUserName()))
                .hasPathSatisfying("$[0].reserver.phoneNumber",
                        p -> p.assertThat().isEqualTo(model1.reserver().getProfile().getPhoneNumber()))
                .hasPathSatisfying("$[0].reserver.contact",
                        p -> p.assertThat().isEqualTo(model1.reserver().getProfile().getContact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnEmpty() throws Exception {

        // given
        when(reservationService.getAll())
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/reservations")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUser(roles = "ADMIN")なし

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/reservations")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
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
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(req.id()))
                .hasPathSatisfying("$.fromDateTime",
                        p -> p.assertThat().isEqualTo(dateTimeFormatter.format(req.fromDateTime())))
                .hasPathSatisfying("$.toDateTime",
                        p -> p.assertThat().isEqualTo(dateTimeFormatter.format(req.toDateTime())))
                .hasPathSatisfying("$.note", p -> p.assertThat().isEqualTo(req.note()))
                .hasPathSatisfying("$.itemId", p -> p.assertThat().isEqualTo(reservation2.getItemId().id()))
                .hasPathSatisfying("$.reserverId", p -> p.assertThat().isEqualTo(reservation2.getReserverId().id()))
                .hasPathSatisfying("$.item.id", p -> p.assertThat().isEqualTo(item3.getId().id()))
                .hasPathSatisfying("$.item.serialNo", p -> p.assertThat().isEqualTo(item3.getSerialNo()))
                .hasPathSatisfying("$.item.itemName", p -> p.assertThat().isEqualTo(item3.getItemName()))
                .hasPathSatisfying("$.reserver.id", p -> p.assertThat().isEqualTo(user2.getId().id()))
                .hasPathSatisfying("$.reserver.loginId", p -> p.assertThat().isEqualTo(user2.getLoginId()))
                .hasPathSatisfying("$.reserver.password", p -> p.assertThat().isEqualTo(user2.getPassword()))
                .hasPathSatisfying("$.reserver.userType", p -> p.assertThat().isEqualTo(user2.getUserType().name()))
                .hasPathSatisfying("$.reserver.userName",
                        p -> p.assertThat().isEqualTo(user2.getProfile().getUserName()))
                .hasPathSatisfying("$.reserver.phoneNumber",
                        p -> p.assertThat().isEqualTo(user2.getProfile().getPhoneNumber()))
                .hasPathSatisfying("$.reserver.contact",
                        p -> p.assertThat().isEqualTo(user2.getProfile().getContact()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnParameterError() throws Exception {

        // given
        ReservationUpdateRequest req = ReservationUpdateRequest.builder().build();
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました",
                        "id",
                        "fromDateTime",
                        "toDateTime");
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
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyText()
                .contains("NOT_FOUND");
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

        // given
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/reservations")
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
    void testUpdateOnAuthenticationError() throws Exception {

        // given
        ReservationUpdateRequest req = createReservationUpdateRequestBuilder().build();
        String requestBody = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(reservationService, never()).update(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete() throws Exception {

        // given
        int reservationId = 1;
        Mockito.doNothing().when(reservationService).delete(new ReservationId(reservationId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/reservations/{id}", reservationId)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnParameterError() throws Exception {

        // given
        int invalidId = -1;

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/reservations/{id}", invalidId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "id");
        verify(reservationService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnNotFound() throws Exception {

        // given
        int reservationId = 999;
        Mockito.doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(reservationService).delete(new ReservationId(reservationId));

        //  when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/reservations/{id}", reservationId)
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
                .uri("/admin/reservations/{id}", reservationId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
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