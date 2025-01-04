package io.extact.msa.spring.domain.reservation.constraint;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;
import io.extact.msa.spring.test.assertj.ConstraintViolationSetAssert;

@SpringBootTest(classes = ValidationConfiguration.class, webEnvironment = WebEnvironment.NONE)
class ToDateTimeTest {

    @Test
    void testValidate(@Autowired Validator validator) {

        Data OK= new Data(LocalDateTime.now());
        Set<ConstraintViolation<Data>> result = validator.validate(OK);
        ConstraintViolationSetAssert.assertThat(result)
            .hasNoViolations();

        // 利用終了日エラー(null)
        Data NG= new Data(null);
        result = validator.validate(NG);
        ConstraintViolationSetAssert.assertThat(result)
            .hasSize(1)
            .hasViolationOnPath("value")
            .hasMessageEndingWith("NotNull.message");
    }

    static record Data(
            @ToDateTime //
            LocalDateTime value) {
    }
}
