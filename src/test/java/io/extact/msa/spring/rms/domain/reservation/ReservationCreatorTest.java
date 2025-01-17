package io.extact.msa.spring.rms.domain.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.framework.model.DefaultModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.InMemoryIdentityGenerator;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationCreator.ReservationModelAttributes;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationCreatorTest {

    private ReservationCreator reservationCreator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {

        @Bean
        @Scope("prototype")
        IdentityGenerator identityGenerator() {
            return new InMemoryIdentityGenerator();
        }
    }

    @BeforeEach
    void beforeEach(@Autowired IdentityGenerator idGenerator, @Autowired ModelValidator validator) {
        this.reservationCreator = new ReservationCreator(
                idGenerator,
                validator,
                new DefaultModelPropertySupportFactory(validator));
    }

    @Test
    void testCreateModel() {

        // given
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = from.plusDays(1);
        ReservationModelAttributes attrs = ReservationModelAttributes.builder()
                .period(new ReservationPeriod(from, to))
                .note("note")
                .itemId(new ItemId(1))
                .reserverId(new UserId(1))
                .build();

        // when
        Reservation reservation = reservationCreator.create(attrs);

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getId()).isEqualTo(new ReservationId(1));
        assertThat(reservation.getPeriod()).isEqualTo(new ReservationPeriod(from, to));
        assertThat(reservation.getNote()).isEqualTo("note");
        assertThat(reservation.getItemId()).isEqualTo(new ItemId(1));
        assertThat(reservation.getReserverId()).isEqualTo(new UserId(1));
    }

    @Test
    void testCreateModelOnValidationError() {

        // given
        LocalDateTime from = LocalDateTime.now().minusDays(1); // 登録時の過去はエラー
        LocalDateTime to = from.minusDays(1); // 開始より過去はエラー
        ReservationModelAttributes attrs = ReservationModelAttributes.builder()
                .period(new ReservationPeriod(from, to))
                .note("note")
                .itemId(null)
                .reserverId(null)
                .build();

        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            reservationCreator.create(attrs);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf(
                        "Reservation.period",
                        "Reservation.period.from",
                        "Reservation.itemId",
                        "Reservation.reserverId");
    }
}
