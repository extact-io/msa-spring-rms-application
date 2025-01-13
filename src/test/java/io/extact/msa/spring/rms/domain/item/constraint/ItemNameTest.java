package io.extact.msa.spring.rms.domain.item.constraint;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfig;
import io.extact.msa.spring.test.assertj.ConstraintViolationSetAssert;

@SpringBootTest(classes = ValidationConfig.class, webEnvironment = WebEnvironment.NONE)
class ItemNameTest {

    @Test
    void testValidate(@Autowired Validator validator) {

        // レンタル品(15文字以内)
        Data OK= new Data("１２３４５６７８９０１２３４５"); // 境界値:OK
        Set<ConstraintViolation<Data>> result = validator.validate(OK);
        ConstraintViolationSetAssert.assertThat(result)
            .hasNoViolations();

        // レンタル品(15文字より大きい)
        Data NG= new Data("1234567890123456"); // 境界値:NG
        result = validator.validate(NG);
        ConstraintViolationSetAssert.assertThat(result)
            .hasSize(1)
            .hasViolationOnPath("value")
            .hasMessageEndingWith("Size.message");
    }

    static record Data(
            @ItemName //
            String value) {
    }
}
