package io.extact.msa.spring.rms.application.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.item.ItemEntity;


/**
 * レンタル品エンティティのテストクラス。
 * 次の観点でテストを作成している。
 * ・testConstructor: コンストラクタが正しく値を設定するか
 * ・testSetters: 各セッターの動作確認
 * ・testFromUser: from メソッドが正しく User モデルをエンティティに変換するか
 * ・testToModel: toModel メソッドが正しくエンティティをモデルに変換するか
 */
class ItemEntityTest {

    private static final ItemCreatable testCreater = new ItemCreatable() {};

    @Test
    void testConstructor() {
        // given
        Integer id = 1;
        String serialNo = "SN12345";
        String itemName = "Laptop";

        // when
        ItemEntity itemEntity = new ItemEntity(id, serialNo, itemName);

        // then
        assertThat(itemEntity).isNotNull();
        assertThat(itemEntity.getId()).isEqualTo(id);
        assertThat(itemEntity.getSerialNo()).isEqualTo(serialNo);
        assertThat(itemEntity.getItemName()).isEqualTo(itemName);
    }

    @Test
    void testSetters() {
        // given
        ItemEntity itemEntity = new ItemEntity();
        Integer id = 1;
        String serialNo = "SN12345";
        String itemName = "Laptop";

        // when
        itemEntity.setId(id);
        itemEntity.setSerialNo(serialNo);
        itemEntity.setItemName(itemName);

        // then
        assertThat(itemEntity.getId()).isEqualTo(id);
        assertThat(itemEntity.getSerialNo()).isEqualTo(serialNo);
        assertThat(itemEntity.getItemName()).isEqualTo(itemName);
    }

    @Test
    void testFromItem() {
        // given
        Item item = testCreater.newInstance(new ItemId(1), "SN12345", "Laptop");

        // when
        ItemEntity itemEntity = ItemEntity.from(item);

        // then
        assertThat(itemEntity).isNotNull();
        assertThat(itemEntity.getId()).isEqualTo(1);
        assertThat(itemEntity.getSerialNo()).isEqualTo("SN12345");
        assertThat(itemEntity.getItemName()).isEqualTo("Laptop");
    }

    @Test
    void testToModel() {
        // given
        ItemEntity itemEntity = new ItemEntity(1, "SN12345", "Laptop");

        // when
        Item iItem = itemEntity.toModel();

        // then
        assertThat(iItem).isNotNull();
        assertThat(iItem.getId().id()).isEqualTo(1);
        assertThat(iItem.getSerialNo()).isEqualTo("SN12345");
        assertThat(iItem.getItemName()).isEqualTo("Laptop");
    }
}
