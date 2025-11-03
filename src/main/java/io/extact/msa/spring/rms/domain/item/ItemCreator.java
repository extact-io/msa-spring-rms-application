package io.extact.msa.spring.rms.domain.item;

import io.extact.msa.spring.platform.fw.domain.model.ModelCreator;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.repository.IdProvider;
import io.extact.msa.spring.rms.domain.item.ItemCreator.ItemModelAttributes;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemCreator implements ModelCreator<Item, ItemModelAttributes> {

    private final IdProvider<ItemId> idProvider;
    private final ModelValidator validator;
    private final ItemCreatable constructorProxy = new ItemCreatable() {};

    public Item create(ItemModelAttributes attrs) {

        ItemId id = idProvider.nextIdentity();
        Item item = constructorProxy.newInstance(id, attrs.serialNo, attrs.itemName);

        item.configure(validator);
        item.verify();

        return item;
    }

    @Builder
    public static class ItemModelAttributes {

        private String serialNo;
        private String itemName;
    }
}
