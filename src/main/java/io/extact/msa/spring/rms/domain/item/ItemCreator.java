package io.extact.msa.spring.rms.domain.item;

import jakarta.validation.Validator;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemCreator {

    private final ItemRepository repository;
    private final Validator validator;
    private final ItemCreatable constructorProxy = new ItemCreatable() {};

    public Item create(ItemModelAttributes attrs) {

        ItemId id = new ItemId(repository.nextIdentity());
        Item item = constructorProxy.newInstance(id, attrs.serialNo, attrs.itemName);

        item.configureValidator(validator);
        item.verify();

        return item;
    }

    @Builder
    public static class ItemModelAttributes {

        private String serialNo;
        private String itemName;
    }
}
