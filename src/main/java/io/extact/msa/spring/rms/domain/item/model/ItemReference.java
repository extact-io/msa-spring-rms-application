package io.extact.msa.spring.rms.domain.item.model;

import io.extact.msa.spring.platform.fw.domain.model.ReferenceModel;

public interface ItemReference extends ReferenceModel {
    ItemId getId();
    String getSerialNo();
    String getItemName();

}