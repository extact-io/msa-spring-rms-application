package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;
import io.extact.msa.spring.rms.test.ConstraintAnnotationAsserter;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class ReservationUpdateRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        int id = 123;
        LocalDateTime fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime toDateTime = LocalDateTime.of(2025, 1, 1, 12, 0);
        String note = "Test Note";

        // when
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .id(id)
                .fromDateTime(fromDateTime)
                .toDateTime(toDateTime)
                .note(note)
                .build();

        // then
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.fromDateTime()).isEqualTo(fromDateTime);
        assertThat(request.toDateTime()).isEqualTo(toDateTime);
        assertThat(request.note()).isEqualTo(note);
    }

    @Test
    void testApplyAnnotationCorrectly() {
        // given
        BeanDescriptor descriptor = validator.getConstraintsForClass(ReservationUpdateRequest.class);

        // then
        ConstraintAnnotationAsserter.asserterTo(descriptor)
                .verifyClassAnnotations(BeforeAfterDateTime.class)
                .verifyPropertyAnnotations("id", RmsId.class)
                .verifyPropertyAnnotations("fromDateTime", FromDateTime.class)
                .verifyPropertyAnnotations("toDateTime", ToDateTime.class)
                .verifyPropertyAnnotations("note", Note.class);
    }
}
