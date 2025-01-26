package io.extact.msa.spring.rms.interfaces.webapi.member;

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

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.ConstraintAnnotationAsserter;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.Note;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReserveItemRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        LocalDateTime fromDateTime = LocalDateTime.now().plusDays(1);
        LocalDateTime toDateTime = fromDateTime.plusDays(1);
        String note = "Test Note";
        int itemId = 456;
        int reserverId = 789;

        // when
        ReserveItemRequest request = ReserveItemRequest.builder()
                .fromDateTime(fromDateTime)
                .toDateTime(toDateTime)
                .note(note)
                .itemId(itemId)
                .reserverId(reserverId)
                .build();

        // then
        assertThat(request.fromDateTime()).isEqualTo(fromDateTime);
        assertThat(request.toDateTime()).isEqualTo(toDateTime);
        assertThat(request.note()).isEqualTo(note);
        assertThat(request.itemId()).isEqualTo(itemId);
        assertThat(request.reserverId()).isEqualTo(reserverId);
    }

    @Test
    void testApplyAnnotationCorrectly() {
        // given
        BeanDescriptor descriptor = validator.getConstraintsForClass(ReserveItemRequest.class);

        // then
        ConstraintAnnotationAsserter.asserterTo(descriptor)
                .verifyClassAnnotations(BeforeAfterDateTime.class)
                .verifyPropertyAnnotations("fromDateTime", FromDateTime.class)
                .verifyPropertyAnnotations("toDateTime", ToDateTime.class)
                .verifyPropertyAnnotations("note", Note.class)
                .verifyPropertyAnnotations("itemId", RmsId.class)
                .verifyPropertyAnnotations("reserverId", RmsId.class);
    }
}
