package io.extact.msa.spring.item.web;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UpdateRentalItemRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidRequest_thenNoViolations() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(1)
                .serialNo("ABC12345")
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenIdIsNull_thenViolationOccurs() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(null) // Invalid
                .serialNo("ABC12345")
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.NotNull.message}");
    }

    @Test
    void whenIdIsLessThanMin_thenViolationOccurs() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(0) // Invalid
                .serialNo("ABC12345")
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Min.message}");
    }

    @Test
    void whenSerialNoIsBlank_thenViolationOccurs() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(1)
                .serialNo("") // Invalid
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.NotBlank.message}");
    }

    @Test
    void whenSerialNoExceedsMaxLength_thenViolationOccurs() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(1)
                .serialNo("1234567890123456") // Invalid
                .itemName("Laptop")
                .build();

        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Size.message}");
    }

    @Test
    void whenItemNameIsBlank_thenViolationOccurs() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(1)
                .serialNo("ABC12345")
                .itemName("") // blank ok
                .build();

        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenItemNameExceedsMaxLength_thenViolationOccurs() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(1)
                .serialNo("ABC12345")
                .itemName("ThisIsAVeryLongItemName") // Invalid
                .build();

        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Size.message}");
    }
}
