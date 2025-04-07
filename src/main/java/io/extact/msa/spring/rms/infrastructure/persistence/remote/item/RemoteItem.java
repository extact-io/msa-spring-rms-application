package io.extact.msa.spring.rms.infrastructure.persistence.remote.item;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.PhysicalEntity;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;

public record RemoteItem(
        Integer id,
        String serialNo,
        String itemName) implements PhysicalEntity<Item>, ItemCreatable {

    public static RemoteItem from(Item model) {
        return new RemoteItem(
                model.getId().id(),
                model.getSerialNo(),
                model.getItemName());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Item toModel(ModelValidator validator) {
        Item item = newInstance(new ItemId(id), serialNo, itemName);
        item.configure(validator);
        return item;
    }
}
