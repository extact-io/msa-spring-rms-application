package io.extact.msa.spring.rms.application.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.rms.application.support.ApplicationCrudSupport;
import io.extact.msa.spring.rms.domain.item.ItemCreator;
import io.extact.msa.spring.rms.domain.item.ItemCreator.ItemModelAttributes;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;

@Service
public class ItemManagementService {

    private final ItemCreator modelCreator;
    private final ItemRepository repository;
    private final ApplicationCrudSupport<Item> support;

    public ItemManagementService(
            ItemCreator modelCreator,
            DuplicateChecker<Item> duplicateChecker,
            ItemRepository repository) {

        this.modelCreator = modelCreator;
        this.repository = repository;
        this.support = new ApplicationCrudSupport<>(duplicateChecker, repository);
    }

    public List<Item> getAll() {
        return support.getAll();
    }

    public Optional<Item> getById(ItemId id) {
        return repository.find(id);
    }

    public Item add(AddItemCommand command) {
        return support.add(() -> this.createModel(command));
    }

    public Item update(UpdateItemCommand command) {
        return support.update(command.id(), item -> this.editModel(item, command));
    }

    public void delete(ItemId id) {
        support.delete(id);
    }


    private Item createModel(AddItemCommand command) {
        ItemModelAttributes attrs = ItemModelAttributes.builder()
                .serialNo(command.serialNo())
                .itemName(command.itemName())
                .build();
        return modelCreator.create(attrs);
    }

    private void editModel(Item item, UpdateItemCommand command) {
        item.editItem(
                command.serialNo(),
                command.itemName());
    }
}
