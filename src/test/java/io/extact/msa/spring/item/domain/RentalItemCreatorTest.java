package io.extact.msa.spring.item.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.extact.msa.spring.item.domain.model.RentalItem;

/**
 * RentalItemCreatorのテストクラス。
 * 次の観点でテストを実装している。
 * ・入出力のフィールドマッピング
 * ・重複なく連続したIDが発番されるかはRepositoryのテストで行っているため、
 *   ここその観点のテストは必要ないためモックを使用している
 */
@ExtendWith(MockitoExtension.class)
class RentalItemCreatorTest {

    @Mock
    private ItemRepository repository;

    @InjectMocks
    private ItemCreator creator;

    @Test
    void testCreate() {
        // given
        String serialNo = "SN12345";
        String itemName = "Laptop";
        int nextId = 1;
        when(repository.nextIdentity()).thenReturn(nextId);

        // when
        Item rentalItem = creator.create(serialNo, itemName);

        // then
        assertThat(rentalItem).isNotNull();
        assertThat(rentalItem.getId()).isNotNull();
        assertThat(rentalItem.getId().id()).isEqualTo(nextId);
        assertThat(rentalItem.getSerialNo()).isEqualTo(serialNo);
        assertThat(rentalItem.getItemName()).isEqualTo(itemName);

        verify(repository).nextIdentity();
    }
}
