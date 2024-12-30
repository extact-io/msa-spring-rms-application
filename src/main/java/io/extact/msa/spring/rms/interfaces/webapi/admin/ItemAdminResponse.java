package io.extact.msa.spring.rms.interfaces.webapi.admin;

import io.extact.msa.spring.rms.domain.item.model.ItemReference;

public record ItemAdminResponse(
        Integer id,
        String serialNo,
        String itemName) {

    static ItemAdminResponse from(ItemReference model) {
        if (model == null) {
            return null;
        }
        return new ItemAdminResponse(
                model.getId().id(),
                model.getSerialNo(),
                model.getItemName());
    }
}
