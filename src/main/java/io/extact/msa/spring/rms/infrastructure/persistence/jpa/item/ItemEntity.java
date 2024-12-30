package io.extact.msa.spring.rms.infrastructure.persistence.jpa.item;

import static jakarta.persistence.AccessType.*;

import jakarta.persistence.Access;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.TableEntity;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Access(FIELD)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString
public class ItemEntity implements TableEntity<Item>, ItemCreatable {

    @Id
    private Integer id;
    private String serialNo;
    private String itemName;

    public static ItemEntity from(Item model) {
        return new ItemEntity(model.getId().id(), model.getSerialNo(), model.getItemName());
    }

    @Override
    public ItemReference toModel() {
        return newInstance(new ItemId(this.id), this.serialNo, this.itemName);
    }
}
