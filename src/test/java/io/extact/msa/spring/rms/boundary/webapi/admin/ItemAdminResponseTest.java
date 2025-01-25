package io.extact.msa.spring.rms.boundary.webapi.admin;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.PersistedTestData;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;

class ItemAdminResponseTest {

    @Test
    void testFrom() {
        // given
        ItemReference item = PersistedTestData.item1;

        // when
        ItemAdminResponse response = ItemAdminResponse.from(item);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(item.getId().id());
        assertThat(response.serialNo()).isEqualTo(item.getSerialNo());
        assertThat(response.itemName()).isEqualTo(item.getItemName());
    }

    @Test
    void testFromNull() {
        // given
        ItemReference ref = null;
        // when
        ItemAdminResponse response = ItemAdminResponse.from(ref);
        // then
        assertThat(response).isNull();
    }
}
