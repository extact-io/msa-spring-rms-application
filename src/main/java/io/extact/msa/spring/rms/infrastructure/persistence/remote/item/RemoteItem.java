package io.extact.msa.spring.rms.infrastructure.persistence.remote.item;

import io.extact.msa.spring.rms.domain.item.model.ItemModelView;

public record RemoteItem(
        Integer id,
        String serialNo,
        String itemName) {

    static RemoteItem from(ItemModelView model) {
        if (model == null) {
            return null;
        }
        return new RemoteItem(
                model.getId().id(),
                model.getSerialNo(),
                model.getItemName());
    }
}
