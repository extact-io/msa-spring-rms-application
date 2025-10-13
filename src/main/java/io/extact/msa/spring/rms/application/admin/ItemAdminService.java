package io.extact.msa.spring.rms.application.admin;

import java.util.ArrayList;
import java.util.List;

import io.extact.msa.spring.platform.core.transaction.ReadOnly;
import io.extact.msa.spring.platform.fw.application.ApplicationCrudSupport;
import io.extact.msa.spring.platform.fw.application.ApplicationService;
import io.extact.msa.spring.platform.fw.application.event.ApplicationServiceEventPublisher;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.rms.application.admin.event.ItemWillBeDeletedEvent;
import io.extact.msa.spring.rms.domain.item.ItemCreator;
import io.extact.msa.spring.rms.domain.item.ItemCreator.ItemModelAttributes;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;

@ApplicationService
public class ItemAdminService {

    private final ItemCreator modelCreator;
    private final ApplicationCrudSupport<Item> support;
    private final ApplicationServiceEventPublisher eventPublisher;

    public ItemAdminService(
            ItemCreator modelCreator,
            DuplicateChecker<Item> duplicateChecker,
            ItemRepository repository,
            ApplicationServiceEventPublisher eventPublisher) {

        this.modelCreator = modelCreator;
        this.support = new ApplicationCrudSupport<>(duplicateChecker, repository);
        this.eventPublisher = eventPublisher;
    }

    @ReadOnly
    public List<ItemModelView> getAll() {
        return new ArrayList<>(support.getAll()); // 型をModelViewに制限するため変換
    }

    public ItemModelView add(ItemAddCommand command) {
        return support.add(() -> this.createModel(command));
    }

    public ItemModelView update(ItemUpdateCommand command) {
        return support.update(command.id(), item -> this.editModel(item, command));
    }

    public void delete(ItemId id) {
        eventPublisher.publish(new ItemWillBeDeletedEvent(id));
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
