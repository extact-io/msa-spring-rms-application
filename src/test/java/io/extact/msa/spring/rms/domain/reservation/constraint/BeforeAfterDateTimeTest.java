package io.extact.msa.spring.rms.domain.reservation.constraint;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.test.assertj.ConstraintViolationSetAssert;

@SpringBootTest(classes = ValidatorConfig.class, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class BeforeAfterDateTimeTest {

    @Test
    void testValidate(@Autowired Validator validator) {

        Data OK = new Data(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        Set<ConstraintViolation<Data>> result = validator.validate(OK);
        ConstraintViolationSetAssert.assertThat(result)
                .hasNoViolations();

        OK = new Data(null, LocalDateTime.now()); // nullなのでチェック対象外でOK
        result = validator.validate(OK);
        ConstraintViolationSetAssert.assertThat(result)
                .hasNoViolations();

        OK = new Data(LocalDateTime.now().minusHours(1), null); // nullなのでチェック対象外でOK
        result = validator.validate(OK);
        ConstraintViolationSetAssert.assertThat(result)
                .hasNoViolations();


        Data NG = new Data(LocalDateTime.now(), LocalDateTime.now().minusHours(1));
        result = validator.validate(NG);
        ConstraintViolationSetAssert.assertThat(result)
                .hasSize(1)
                .hasMessageEndingWith("bv.BeforeAfterDateTime.message");
    }

    @BeforeAfterDateTime
    static record Data(
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime) implements BeforeAfterDateTimeValidatable {

        @Override
        public LocalDateTime getFrom() {
            return this.fromDateTime;
        }

        @Override
        public LocalDateTime getTo() {
            return this.toDateTime;
        }
    }
}
