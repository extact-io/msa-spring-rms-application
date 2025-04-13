package io.extact.msa.spring.rms.domain.item.model;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;

class ItemModelViewTest {

    private static final ItemCreatable testCreater = new ItemCreatable() {
    };

    @Test
    void testIsEqualNull() {
        assertThat(item1.isEqual(null)).isFalse();
    }

    @Test
    void testIsEqual() {
        assertThat(item1.isEqual(testCreater.newInstance(
                null,
                null,
                null //
                ))).isFalse();
        assertThat(item1.isEqual(testCreater.newInstance(
                item1.getId(),
                null,
                null //
                ))).isFalse();
        assertThat(item1.isEqual(testCreater.newInstance(
                item1.getId(),
                item1.getSerialNo(),
                null //
                ))).isFalse();
        assertThat(item1.isEqual(testCreater.newInstance(
                item1.getId(),
                item1.getSerialNo(),
                item1.getItemName() //
                ))).isTrue();
    }
}
