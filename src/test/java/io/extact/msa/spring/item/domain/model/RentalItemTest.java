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
 * RentalItemのテストクラス。
 * 次の観点でテストを実装している。
 * ・入出力に使っているフィールドマッピング
 * ・各フィールドに対するすべてのバリデーションパターン
 */
class RentalItemTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenReconstructWithValidData_thenNoViolations() {
        // given
        int id = 1;
        String serialNo = "ABC12345";
        String itemName = "Laptop";

        // when
        Item rentalItem = Item.reconstruct(id, serialNo, itemName);

        // then
        Set<ConstraintViolation<Item>> violations = validator.validate(rentalItem);
        assertThat(violations).isEmpty();
        assertThat(rentalItem.getId().id()).isEqualTo(id);
        assertThat(rentalItem.getSerialNo()).isEqualTo(serialNo);
        assertThat(rentalItem.getItemName()).isEqualTo(itemName);
    }

    @Test
    void whenReconstructWithInvalidId_thenViolationOccurs() {
        // given
        int id = 0; // Invalid
        String serialNo = "ABC12345";
        String itemName = "Laptop";

        // when
        Item rentalItem = Item.reconstruct(id, serialNo, itemName);

        // then
        Set<ConstraintViolation<Item>> violations = validator.validate(rentalItem);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Min.message}");
    }

    @Test
    void whenEditSerialNoAndName_thenFieldsAreUpdated() {
        // given
        Item rentalItem = Item.reconstruct(1, "ABC12345", "Laptop");
        String newSerialNo = "XYZ67890";
        String newItemName = "Tablet";

        // when
        rentalItem.editSerialNoAndName(newSerialNo, newItemName);

        // then
        assertThat(rentalItem.getSerialNo()).isEqualTo(newSerialNo);
        assertThat(rentalItem.getItemName()).isEqualTo(newItemName);
    }

    @Test
    void whenEditSerialNoAndNameWithInvalidSerialNo_thenViolationOccurs() {
        // given
        Item rentalItem = Item.reconstruct(1, "ABC12345", "Laptop");
        String invalidSerialNo = ""; // Invalid
        String newItemName = "Tablet";

        // when
        rentalItem.editSerialNoAndName(invalidSerialNo, newItemName);

        // then
        Set<ConstraintViolation<Item>> violations = validator.validate(rentalItem);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.NotBlank.message}");
    }
}
