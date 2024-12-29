package io.extact.msa.spring.item.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * ItemIdTestのテストクラス。
 * 次の観点でテストを実装している。
 * ・入出力に使っているフィールドマッピング
 * ・各フィールドに対するすべてのバリデーションパターン
 */
class ItemIdTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenGettingId_thenExpectedValueIsReturned() {
        // given
        int expectedId = 42;

        // when
        ItemId itemId = new ItemId(expectedId);

        // then
        assertThat(itemId.id()).isEqualTo(expectedId);
    }

    @Test
    void whenValidId_thenNoViolations() {
        // given
        ItemId itemId = new ItemId(1);

        // when
        Set<ConstraintViolation<ItemId>> violations = validator.validate(itemId);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void whenIdIsZero_thenViolationOccurs() {
        // given
        ItemId itemId = new ItemId(0); // Invalid, id must be >= 1

        // when
        Set<ConstraintViolation<ItemId>> violations = validator.validate(itemId);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Min.message}");
    }

    @Test
    void whenIdIsNegative_thenViolationOccurs() {
        // given
        ItemId itemId = new ItemId(-1); // Invalid, id must be >= 1

        // when
        Set<ConstraintViolation<ItemId>> violations = validator.validate(itemId);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Min.message}");
    }
}
