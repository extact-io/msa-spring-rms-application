package io.extact.msa.spring.rms.domain.item.model;

import java.util.Objects;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;

public interface ItemModelView extends EntityModelView<ItemModelView> {

    ItemId getId();

    String getSerialNo();

    String getItemName();

    @Override
    default boolean isEqual(ItemModelView other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(getId(), other.getId())
                && Objects.equals(getSerialNo(), other.getSerialNo())
                && Objects.equals(getItemName(), other.getItemName());
    }
}