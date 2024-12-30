package io.extact.msa.spring.item.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegisterRentalItemCommandTest {

    @Test
    void testBuilder() {

        ItemAddCommand command = ItemAddCommand.builder()
                .serialNo("serialNo123")
                .itemName("itemNameABC")
                .build();

        assertThat(command.serialNo()).isEqualTo("serialNo123");
        assertThat(command.itemName()).isEqualTo("itemNameABC");
    }
}