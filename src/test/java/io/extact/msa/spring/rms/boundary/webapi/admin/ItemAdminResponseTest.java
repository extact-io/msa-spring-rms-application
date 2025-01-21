package io.extact.msa.spring.rms.boundary.webapi.admin;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;

class ItemAdminResponseTest {

    private static final ItemCreatable testCreator = new ItemCreatable() {};

    @Test
    void from_shouldMapFieldsCorrectly() {
        // given
        ItemReference ref = testCreator.newInstance(new ItemId(1), "12345", "Test Item");

        // when
        ItemAdminResponse response = ItemAdminResponse.from(ref);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.serialNo()).isEqualTo("12345");
        assertThat(response.itemName()).isEqualTo("Test Item");
    }

    @Test
    void from_shouldReturnNullWhenModelIsNull() {
        // given
        ItemReference ref = null;

        // when
        ItemAdminResponse response = ItemAdminResponse.from(ref);

        // then
        assertThat(response).isNull();
    }
}
