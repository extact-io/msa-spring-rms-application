package io.extact.msa.spring.rms.webapi.member;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.PersistedTestData;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;
import io.extact.msa.spring.rms.webapi.member.ItemResponse;

class ItemResponseTest {

    @Test
    void testFrom() {
        // given
        ItemModelView item = PersistedTestData.item1;

        // when
        ItemResponse response = ItemResponse.from(item);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(item.getId().id());
        assertThat(response.serialNo()).isEqualTo(item.getSerialNo());
        assertThat(response.itemName()).isEqualTo(item.getItemName());
    }

    @Test
    void testFromNull() {
        // given
        ItemModelView view = null;
        // when
        ItemResponse response = ItemResponse.from(view);
        // then
        assertThat(response).isNull();
    }
}
