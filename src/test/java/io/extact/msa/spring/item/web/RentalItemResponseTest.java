package io.extact.msa.spring.item.web;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.item.domain.model.RentalItem;

class RentalItemResponseTest {

    @Test
    void from_shouldMapFieldsCorrectly() {

        // Arrange
        Item model = Item.reconstruct(1, "12345", "Test Item");

        // Act
        ItemMemberResponse response = ItemMemberResponse.from(model);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.serialNo()).isEqualTo("12345");
        assertThat(response.itemName()).isEqualTo("Test Item");
    }

    @Test
    void from_shouldReturnNullWhenModelIsNull() {
        // Arrange
        Item model = null;

        // Act
        ItemMemberResponse response = ItemMemberResponse.from(model);

        // Assert
        assertThat(response).isNull();
    }
}
