package io.extact.msa.spring.rms.infrastructure.persistence.remote.item;

import io.extact.msa.spring.rms.domain.item.model.ItemModelView;

public record RemoteItemResponse(
        Integer id,
        String serialNo,
        String itemName) {

    static RemoteItemResponse from(ItemModelView model) {
        if (model == null) {
            return null;
        }
        return new RemoteItemResponse(
                model.getId().id(),
                model.getSerialNo(),
                model.getItemName());
    }
}
