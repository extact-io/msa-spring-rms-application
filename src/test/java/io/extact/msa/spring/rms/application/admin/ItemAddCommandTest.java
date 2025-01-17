package io.extact.msa.spring.rms.application.admin;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ItemAddCommandTest {

    @Test
    void testBuilder() {
        // given
        String serialNo = "serialNo";
        String itemName = "itemName";

        // when
        ItemAddCommand command = ItemAddCommand.builder()
                .serialNo(serialNo)
                .itemName(itemName)
                .build();

        // then
        assertThat(command.serialNo()).isEqualTo(serialNo);
        assertThat(command.itemName()).isEqualTo(itemName);
    }
}