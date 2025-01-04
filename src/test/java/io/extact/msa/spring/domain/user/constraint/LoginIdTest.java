package io.extact.msa.spring.domain.user.constraint;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.rms.domain.user.constraints.LoginId;
import io.extact.msa.spring.test.assertj.ConstraintViolationSetAssert;

@SpringBootTest(classes = ValidationConfiguration.class, webEnvironment = WebEnvironment.NONE)
class LoginIdTest {

    @Test
    void testValidate(@Autowired Validator validator) {

        Data OK= new Data("abcdefg");
        Set<ConstraintViolation<Data>> result = validator.validate(OK);
        ConstraintViolationSetAssert.assertThat(result)
            .hasNoViolations();

        // ログインIDエラー(null)
        Data NG= new Data(null);
        result = validator.validate(NG);
        ConstraintViolationSetAssert.assertThat(result)
            .hasSize(1)
            .hasViolationOnPath("value")
            .hasMessageEndingWith("NotNull.message");

        // ログインIDエラー(空文字列)
        NG= new Data("");
        result = validator.validate(NG);
        ConstraintViolationSetAssert.assertThat(result)
            .hasSize(1)
            .hasViolationOnPath("value")
            .hasMessageEndingWith("Size.message");

        // ログインIDエラー(5文字未満)
        NG= new Data("1234");
        result = validator.validate(NG);
        ConstraintViolationSetAssert.assertThat(result)
            .hasSize(1)
            .hasViolationOnPath("value")
            .hasMessageEndingWith("Size.message");

        // ログインIDエラー(10文字より以上)
        NG= new Data("12345678901");
        result = validator.validate(NG);
        ConstraintViolationSetAssert.assertThat(result)
            .hasSize(1)
            .hasViolationOnPath("value")
            .hasMessageEndingWith("Size.message");
    }

    static record Data(
            @LoginId //
            String value) {
    }
}
