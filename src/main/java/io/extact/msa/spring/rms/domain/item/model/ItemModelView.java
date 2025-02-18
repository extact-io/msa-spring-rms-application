package io.extact.msa.spring.rms.domain.item.model;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;

public interface ItemModelView extends EntityModelView {

    ItemId getId();
    String getSerialNo();
    String getItemName();
}