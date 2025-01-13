package io.extact.msa.spring.rms.domain.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.file.FileRepositoryConfig;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("reservation-file")
class ReservationDuplicateCheckerTest {

    @Autowired
    private ReservationDuplicateChecker duplicateChecker;

    @Configuration(proxyBeanMethods = false)
    @Import(FileRepositoryConfig.class)
    static class TestConfig {

        @Bean
        ReservationDuplicateChecker reservationDuplicateChecker(ReservationRepository repository) {
            return new ReservationDuplicateChecker(repository);
        }
    }

    @Test
    void testCheckOK() {

        // given
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = from.plusDays(1);

        ReservationCreatable constructorProxy = new ReservationCreatable() {};

        Reservation reservation = constructorProxy.newInstance(
                new ReservationId(1),
                new ReservationPeriod(from, to),
                "note", new ItemId(1),
                new UserId(1));

        // when
        assertThatCode(() -> {
            duplicateChecker.check(reservation);
        })
        // then
        .doesNotThrowAnyException();
    }

    @Test
    void testCheckNG() {

        // given
        LocalDateTime from = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2099, 12, 31, 23, 59);

        ReservationCreatable constructorProxy = new ReservationCreatable() {};

        Reservation reservation = constructorProxy.newInstance(
                new ReservationId(1),
                new ReservationPeriod(from, to),
                "note", new ItemId(3),
                new UserId(1));

        // when
        BusinessFlowException e = assertThrows(BusinessFlowException.class, () -> {
            duplicateChecker.check(reservation);
        });
        // then
        assertThat(e.getCauseType()).isEqualTo(CauseType.DUPLICATE);
    }
}
