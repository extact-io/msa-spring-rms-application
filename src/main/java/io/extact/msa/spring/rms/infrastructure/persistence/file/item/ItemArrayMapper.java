package io.extact.msa.spring.rms.infrastructure.persistence.file.item;

import io.extact.msa.spring.platform.fw.exception.RmsSystemException;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;

public class ItemArrayMapper implements ModelArrayMapper<Item>, ItemCreatable {

    public static final ItemArrayMapper INSTANCE = new ItemArrayMapper();

    @Override
    public Item toModel(String[] attributes) throws RmsSystemException {
        Integer id = Integer.parseInt(attributes[0]);
        String serialNo = attributes[1];
        String itemName = attributes[2];
        return newInstance(new ItemId(id), serialNo, itemName);
    }

    @Override
    public String[] toArray(Item item) {
        String[] attributes = new String[3];
        attributes[0] = String.valueOf(item.getId().id());
        attributes[1] = item.getSerialNo();
        attributes[2] = item.getItemName();
        return attributes;
    }
}
