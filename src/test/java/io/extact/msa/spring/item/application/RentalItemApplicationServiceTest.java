package io.extact.msa.spring.item.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.extact.msa.spring.item.domain.RentalItemCreator;
import io.extact.msa.spring.item.domain.RentalItemRepository;
import io.extact.msa.spring.item.domain.model.ItemId;
import io.extact.msa.spring.item.domain.model.RentalItem;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;

class RentalItemApplicationServiceTest {

    @Mock
    private ItemCreator factory;

    @Mock
    private DuplicateChecker<Item> duplicateChecker;

    @Mock
    private ItemRepository repository;

    @InjectMocks
    private RentalItemApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_ShouldRegisterRentalItem() {
        // given
        AddItemCommand command = new AddItemCommand("SN123", "Item Name");
        Item newItem = Item.reconstruct(1, "SN123", "Item Name");

        when(factory.create(command.serialNo(), command.itemName())).thenReturn(newItem);
        doNothing().when(duplicateChecker).check(newItem);
        doNothing().when(repository).add(newItem);

        // when
        Item result = service.register(command);

        // then
        assertThat(result).isEqualTo(newItem);
        verify(factory).create(command.serialNo(), command.itemName());
        verify(duplicateChecker).check(newItem);
        verify(repository).add(newItem);
    }

    @Test
    void testEdit_ShouldEditRentalItem() {
        // given
        UpdateItemCommand command = new UpdateItemCommand(new ItemId(1), "SN456", "Updated Name");
        Item existingItem = Item.reconstruct(1, "SN123", "Item Name");

        when(repository.find(command.id())).thenReturn(Optional.of(existingItem));
        doNothing().when(duplicateChecker).check(existingItem);
        doNothing().when(repository).update(existingItem);

        // when
        Item result = service.edit(command);

        // then
        assertThat(result).isEqualTo(existingItem);
        assertThat(result.getSerialNo()).isEqualTo("SN456");
        assertThat(result.getItemName()).isEqualTo("Updated Name");
        verify(repository).find(command.id());
        verify(duplicateChecker).check(existingItem);
        verify(repository).update(existingItem);
    }

    @Test
    void testEdit_ShouldThrowExceptionIfRentalItemNotFound() {
        // given
        UpdateItemCommand command = new UpdateItemCommand(new ItemId(99), "SN999", "Non-existent Item");

        when(repository.find(command.id())).thenReturn(Optional.empty());

        // when & then
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.edit(command);
        });

        assertThat(exception.getMessage()).isEqualTo("target does not exist for id");
        verify(repository).find(command.id());
    }

    @Test
    void testRegister_ShouldThrowExceptionForDuplicateItem() {
        // given
        AddItemCommand command = new AddItemCommand("SN123", "Duplicate Item");
        Item duplicateItem = Item.reconstruct(1, "SN123", "Duplicate Item");

        when(factory.create(command.serialNo(), command.itemName())).thenReturn(duplicateItem);
        doThrow(new BusinessFlowException("The name is already registered.", CauseType.DUPLICATE))
                .when(duplicateChecker).check(duplicateItem);

        // when & then
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.register(command);
        });

        assertThat(exception.getMessage()).isEqualTo("The name is already registered.");
        verify(factory).create(command.serialNo(), command.itemName());
        verify(duplicateChecker).check(duplicateItem);
    }
}
