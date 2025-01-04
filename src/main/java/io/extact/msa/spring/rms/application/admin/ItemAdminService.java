package io.extact.msa.spring.rms.application.admin;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import io.extact.msa.spring.platform.fw.application.ApplicationCrudSupport;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.rms.domain.item.ItemCreator;
import io.extact.msa.spring.rms.domain.item.ItemCreator.ItemModelAttributes;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;

@Transactional
public class ItemAdminService {

    private final ItemCreator modelCreator;
    private final ApplicationCrudSupport<Item> support;

    public ItemAdminService(
            ItemCreator modelCreator,
            DuplicateChecker<Item> duplicateChecker,
            ItemRepository repository) {

        this.modelCreator = modelCreator;
        this.support = new ApplicationCrudSupport<>(duplicateChecker, repository);
    }

    public List<? extends ItemReference> getAll() {
        return support.getAll();
    }

    public ItemReference add(ItemAddCommand command) {
        return support.add(() -> this.createModel(command));
    }

    public ItemReference update(ItemUpdateCommand command) {
        return support.update(command.id(), item -> this.editModel(item, command));
    }

    public void delete(ItemId id) {
        support.delete(id);
    }


    private Item createModel(ItemAddCommand command) {
        ItemModelAttributes attrs = ItemModelAttributes.builder()
                .serialNo(command.serialNo())
                .itemName(command.itemName())
                .build();
        return modelCreator.create(attrs);
    }

    private void editModel(Item item, ItemUpdateCommand command) {
        item.editItem(
                command.serialNo(),
                command.itemName());
    }
}
