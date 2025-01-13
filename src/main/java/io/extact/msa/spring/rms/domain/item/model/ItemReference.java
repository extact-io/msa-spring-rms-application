package io.extact.msa.spring.rms.domain.item.model;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelReference;

public interface ItemReference extends EntityModelReference {

    ItemId getId();
    String getSerialNo();
    String getItemName();
}