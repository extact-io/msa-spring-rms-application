package io.extact.msa.spring.rms.interfaces.webapi.member;

import io.extact.msa.spring.rms.domain.item.model.ItemReference;

public record ItemMemberResponse(
        Integer id,
        String serialNo,
        String itemName) {

    static ItemMemberResponse from(ItemReference model) {
        if (model == null) {
            return null;
        }
        return new ItemMemberResponse(
                model.getId().id(),
                model.getSerialNo(),
                model.getItemName());
    }
}
