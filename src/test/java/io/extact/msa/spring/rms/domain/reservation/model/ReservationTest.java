package io.extact.msa.spring.rms.domain.reservation.model;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.framework.model.DefaultModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.ConstraintAnnotationAsserter;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTimeFuture;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;
import io.extact.msa.spring.rms.domain.user.model.UserId;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationTest {

    @Autowired
    private ModelValidator modelValidator;
    @Autowired
    private Validator beanValidator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testApplyAnnotationCorrectly() {

        BeanDescriptor bd = beanValidator.getConstraintsForClass(ReservationId.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", RmsId.class);

        bd = beanValidator.getConstraintsForClass(Reservation.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", NotNull.class)
                .verifyPropertyAnnotations("period", NotNull.class)
                .verifyPropertyAnnotations("note", Note.class)
                .verifyPropertyAnnotations("itemId", NotNull.class)
                .verifyPropertyAnnotations("reserverId", NotNull.class);

        bd = beanValidator.getConstraintsForClass(ReservationPeriod.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyClassAnnotations(BeforeAfterDateTime.class)
                .verifyPropertyAnnotations("from", FromDateTime.class, FromDateTimeFuture.class)
                .verifyPropertyAnnotations("to", ToDateTime.class);
    }

    @Test
    void testEditReservationOK() {
        // geven
        Reservation reservation = newReservation();
        ReservationId oldId = reservation.getId();
        ReservationPeriod newPeriod = new ReservationPeriod(
                LocalDateTime.now().minusDays(1), // 更新は過去日でもエラーにならないこと
                LocalDateTime.now().plusDays(1));
        String newNote = "newNote";
        ItemId oldItemId = reservation.getItemId();
        UserId oldUserId = reservation.getReserverId();

        // when
        assertThatCode(() -> {
            reservation.editReservation(newPeriod, newNote);
        })
        // then
        .doesNotThrowAnyException();

        // 変更箇所が反映されていること
        assertThat(reservation.getPeriod()).isEqualTo(newPeriod);
        assertThat(reservation.getNote()).isEqualTo(newNote);
        // 関係ない個所に副作用が発生していないこと
        assertThat(reservation.getId()).isEqualTo(oldId);
        assertThat(reservation.getItemId()).isEqualTo(oldItemId);
        assertThat(reservation.getReserverId()).isEqualTo(oldUserId);
    }

    @Test
    void testEditReservationNG() {
        // geven
        Reservation reservation = newReservation();

        ReservationPeriod oldPeriod = reservation.getPeriod();
        String oldNote = reservation.getNote();

        ReservationPeriod newPeriod = new ReservationPeriod(
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(1)); // 前後逆転
        String newNote = "newNote";


        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            reservation.editReservation(newPeriod, newNote);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf("Reservation.period"); // 最初に検知の1つだけ

        // モデルが更新されてないこと
        assertThat(reservation.getPeriod()).isEqualTo(oldPeriod);
        assertThat(reservation.getNote()).isEqualTo(oldNote);
    }

    private Reservation newReservation() {
        Reservation r = new Reservation(
                new ReservationId(1),
                new ReservationPeriod(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1)),
                "note",
                new ItemId(1),
                new UserId(1));
        r.configureSupport(new DefaultModelPropertySupportFactory(modelValidator));
        return r;
    }
}
