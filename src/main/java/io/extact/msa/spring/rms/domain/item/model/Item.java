package io.extact.msa.spring.rms.domain.item.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.extact.msa.spring.platform.fw.domain.model.EntityModel;
import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupport;
import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.rms.domain.item.constraint.ItemName;
import io.extact.msa.spring.rms.domain.item.constraint.SerialNo;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
public class Item implements EntityModel, ItemReference {

    @Getter
    @NotNull
    @Valid
    private ItemId id;
    @Getter
    @SerialNo
    private String serialNo;
    @Getter
    @ItemName
    private String itemName;

    private ModelPropertySupport modelSupport;

    @Override
    public void configureValidator(ModelValidator validator) {
        //this.modelSupport = new DefaultModelSetterSupport<>(Item::new, validator, this);
    }

    Item(ItemId id, String serialNo, String itemName) {
        this.id = id;
        this.serialNo = serialNo;
        this.itemName = itemName;
    }

    public void editItem(String serialNo, String itemName) {
        setSerialNo(serialNo);
        setItemName(itemName);
    }

    private void setSerialNo(String newValue) {
        modelSupport.setPropertyWithValidation("serialNo", newValue);
    }

    private void setItemName(String newValue) {
        modelSupport.setPropertyWithValidation("itemName", newValue);
    }

    public interface ItemCreatable {
        default Item newInstance(
                ItemId id,
                String serialNo,
                String itemName) {
            return new Item(id, serialNo, itemName);
        }
    }

    public void configureSupport(ModelPropertySupportFactory<Item> modelSupportFactory) {
        this.modelSupport = modelSupportFactory.create(Item::new, this);
    }
}
