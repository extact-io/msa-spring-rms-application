package io.extact.msa.spring.rms.domain.item.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.extact.msa.spring.platform.fw.domain.model.EntityModel;
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

    @NotNull
    @Valid
    @Getter
    private ItemId id;
    @SerialNo
    @Getter
    private String serialNo;
    @ItemName
    @Getter
    private String itemName;

    private ModelValidator validator;

    Item(ItemId id, String serialNo, String itemName) {
        this.id = id;
        this.serialNo = serialNo;
        this.itemName = itemName;
    }

    public void editItem(String serialNo, String itemName) {
        setSerialNo(serialNo);
        setItemName(itemName);
    }

    @Override
    public void configureValidator(ModelValidator validator) {
        this.validator = validator;
    }

    private void setSerialNo(String newValue) {
        Item test = new Item();
        test.serialNo = newValue;
        validator.validateField(test, "serialNo");

        this.serialNo = newValue;
    }

    private void setItemName(String newValue) {
        Item test = new Item();
        test.itemName = newValue;
        validator.validateField(test, "itemName");

        this.itemName = newValue;
    }

    public interface ItemCreatable {
        default Item newInstance(
                ItemId id,
                String serialNo,
                String itemName) {
            return new Item(id, serialNo, itemName);
        }
    }
}
