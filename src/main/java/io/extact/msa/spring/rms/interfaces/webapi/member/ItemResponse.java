package io.extact.msa.spring.rms.interfaces.webapi.member;

import io.extact.msa.spring.rms.domain.item.model.ItemModelView;

public record ItemResponse(
        Integer id,
        String serialNo,
        String itemName) {

    static ItemResponse from(ItemModelView model) {
        if (model == null) {
            return null;
        }
        return new ItemResponse(
                model.getId().id(),
                model.getSerialNo(),
                model.getItemName());
    }
}
