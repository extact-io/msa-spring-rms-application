package io.extact.msa.spring.rms.domain.item.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationGroups.Add;
import io.extact.msa.spring.platform.fw.domain.model.DomainModel;
import io.extact.msa.spring.platform.fw.exception.RmsConstraintViolationException;
import io.extact.msa.spring.rms.domain.item.constraint.ItemName;
import io.extact.msa.spring.rms.domain.item.constraint.SerialNo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@Getter
@ToString
public class Item implements DomainModel, ItemReference {

    private @NotNull @Valid ItemId id;
    private @SerialNo String serialNo;
    private @ItemName String itemName;

    private Validator validator;

    Item(ItemId id, String serialNo, String itemName) {
        this.id = id;
        this.serialNo = serialNo;
        this.itemName = itemName;
    }

    public void editItem(String serialNo, String itemName) {
        this.serialNo = serialNo;
        this.itemName = itemName;
    }

    @Override
    public void configureValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void verify() {
        // 生々しいのでvalidatorをなにかで被せる
        Set<ConstraintViolation<Item>> result = this.validator.validate(this, Default.class, Add.class);
        if (!result.isEmpty()) {
            throw new RmsConstraintViolationException("validation error.", new HashSet<>(result));
        }
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
