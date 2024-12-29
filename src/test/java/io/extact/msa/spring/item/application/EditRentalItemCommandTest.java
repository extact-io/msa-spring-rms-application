package io.extact.msa.spring.item.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.item.domain.model.ItemId;

class EditRentalItemCommandTest {

    @Test
    void testBuilder() {

        UpdateItemCommand command = UpdateItemCommand.builder()
                .id(new ItemId(1))
                .serialNo("serialNo")
                .itemName("itemName")
                .build();

        assertThat(command.id()).isEqualTo(new ItemId(1));
        assertThat(command.serialNo()).isEqualTo("serialNo");
        assertThat(command.itemName()).isEqualTo("itemName");
    }
}
