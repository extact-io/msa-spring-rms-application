package io.extact.msa.spring.item.web;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AddRentalItemRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidRequest_thenNoViolations() {
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("ABC12345")
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemAddRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenSerialNoIsBlank_thenViolationOccurs() {
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("") // Invalid
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemAddRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.NotBlank.message}");
    }

    @Test
    void whenSerialNoExceedsMaxLength_thenViolationOccurs() {
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("1234567890123456") // 16 characters, exceeds max length of 15
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemAddRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Size.message}");
    }

    @Test
    void whenItemNameIsBlank_thenViolationOccurs() {
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("ABC12345")
                .itemName("") // Blank OK
                .build();

        Set<ConstraintViolation<ItemAddRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenItemNameExceedsMaxLength_thenViolationOccurs() {
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("ABC12345")
                .itemName("ThisIsAVeryLongItemName") // Exceeds 15 characters
                .build();

        Set<ConstraintViolation<ItemAddRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Size.message}");
    }
}
