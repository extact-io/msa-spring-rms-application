package io.extact.msa.spring.item.infrastructure.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.item.domain.model.RentalItem;

class RentalItemEntityTest {

    @Test
    void testConstructor() {
        // given
        Integer id = 1;
        String serialNo = "SN12345";
        String itemName = "Laptop";

        // when
        ItemEntity rentalItemEntity = new ItemEntity(id, serialNo, itemName);

        // then
        assertThat(rentalItemEntity).isNotNull();
        assertThat(rentalItemEntity.getId()).isEqualTo(id);
        assertThat(rentalItemEntity.getSerialNo()).isEqualTo(serialNo);
        assertThat(rentalItemEntity.getItemName()).isEqualTo(itemName);
    }

    @Test
    void testSetters() {
        // given
        ItemEntity rentalItemEntity = new ItemEntity();
        Integer id = 1;
        String serialNo = "SN12345";
        String itemName = "Laptop";

        // when
        rentalItemEntity.setId(id);
        rentalItemEntity.setSerialNo(serialNo);
        rentalItemEntity.setItemName(itemName);

        // then
        assertThat(rentalItemEntity.getId()).isEqualTo(id);
        assertThat(rentalItemEntity.getSerialNo()).isEqualTo(serialNo);
        assertThat(rentalItemEntity.getItemName()).isEqualTo(itemName);
    }

    @Test
    void testFromRentalItem() {
        // given
        Item rentalItem = Item.reconstruct(1, "SN12345", "Laptop");

        // when
        ItemEntity rentalItemEntity = ItemEntity.from(rentalItem);

        // then
        assertThat(rentalItemEntity).isNotNull();
        assertThat(rentalItemEntity.getId()).isEqualTo(1);
        assertThat(rentalItemEntity.getSerialNo()).isEqualTo("SN12345");
        assertThat(rentalItemEntity.getItemName()).isEqualTo("Laptop");
    }

    @Test
    void testToModel() {
        // given
        ItemEntity rentalItemEntity = new ItemEntity(1, "SN12345", "Laptop");

        // when
        Item rentalItem = rentalItemEntity.toModel();

        // then
        assertThat(rentalItem).isNotNull();
        assertThat(rentalItem.getId().id()).isEqualTo(1);
        assertThat(rentalItem.getSerialNo()).isEqualTo("SN12345");
        assertThat(rentalItem.getItemName()).isEqualTo("Laptop");
    }
}
