package io.extact.msa.spring.rms.domain.item;

import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemCreator {

    private final IdentityGenerator idGenerator;
    private final ModelValidator validator;
    private final ModelPropertySupportFactory modelSupportFactory;
    private final ItemCreatable constructorProxy = new ItemCreatable() {};

    public Item create(ItemModelAttributes attrs) {

        ItemId id = new ItemId(idGenerator.nextIdentity());
        Item item = constructorProxy.newInstance(id, attrs.serialNo, attrs.itemName);

        item.configureSupport(modelSupportFactory);
        validator.validateModel(item);

        return item;
    }

    @Builder
    public static class ItemModelAttributes {

        private String serialNo;
        private String itemName;
    }
}
