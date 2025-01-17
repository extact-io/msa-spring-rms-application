package io.extact.msa.spring.rms.application.admin;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.ItemId;


class ItemUpdateCommandTest {
    @Test
    void testBuilder() {
        // given
        ItemId itemId = new ItemId(1);
        String serialNo = "serialNo";
        String itemName = "itemName";

        // when
        ItemUpdateCommand command = ItemUpdateCommand.builder()
                .id(itemId)
                .serialNo(serialNo)
                .itemName(itemName)
                .build();

        // then
        assertThat(command.id()).isEqualTo(itemId);
        assertThat(command.serialNo()).isEqualTo(serialNo);
        assertThat(command.itemName()).isEqualTo(itemName);
    }
}
