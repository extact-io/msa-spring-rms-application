package io.extact.msa.spring.rms.infrastructure.persistence.remote.item;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.PhysicalEntity;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;

public record RemoteItem(
        Integer id,
        String serialNo,
        String itemName) implements PhysicalEntity<Item> {

    static RemoteItem from(ItemModelView model) {
        if (model == null) {
            return null;
        }
        return new RemoteItem(
                model.getId().id(),
                model.getSerialNo(),
                model.getItemName());
    }

    @Override
    public Integer getId() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Item toModel(ModelValidator validator) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }
}
