package io.extact.msa.spring.rms.domain.item.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.extact.msa.spring.platform.fw.domain.model.AbstractEntityModel;
import io.extact.msa.spring.rms.domain.item.constraint.ItemName;
import io.extact.msa.spring.rms.domain.item.constraint.SerialNo;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString
public class Item extends AbstractEntityModel implements ItemModelView {

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

    Item(ItemId id, String serialNo, String itemName) {
        this.id = id;
        this.serialNo = serialNo;
        this.itemName = itemName;
    }

    // --------------------------------------- service methods

    public void editItem(String newSerialNo, String newItemName) {
        applySerialNo(newSerialNo);
        applyItemName(newItemName);
    }

    // --------------------------------------- private methods

    private void applySerialNo(String newSerialNo) {
        Item test = new Item();
        test.serialNo = newSerialNo;
        validator().validateField(test, test::getSerialNo);
        this.serialNo = newSerialNo;
    }

    private void applyItemName(String newItemName) {
        Item test = new Item();
        test.itemName = newItemName;
        validator().validateField(test, test::getItemName);
        this.itemName = newItemName;
    }

    // --------------------------------------- inner interface

    public interface ItemCreatable {
        default Item newInstance(
                ItemId id,
                String serialNo,
                String itemName) {
            return new Item(id, serialNo, itemName);
        }
    }
}
