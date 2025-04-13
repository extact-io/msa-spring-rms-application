package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItem;


/**
 * RemoteItemのテストクラス。
 * 次の観点でテストを作成している。
 * ・testConstructor: コンストラクタが正しく値を設定するか
 * ・testFromUser: from メソッドが正しく Item モデルをエンティティに変換するか
 * ・testToModel: toModel メソッドが正しくエンティティをモデルに変換するか
 */
class RemoteItemTest {

    private static final ItemCreatable testCreater = new ItemCreatable() {};

    @Test
    void testConstructor() {
        // given
        Integer id = 1;
        String serialNo = "SN12345";
        String itemName = "Laptop";

        // when
        RemoteItem remoteItem = new RemoteItem(id, serialNo, itemName);

        // then
        assertThat(remoteItem).isNotNull();
        assertThat(remoteItem.id()).isEqualTo(id);
        assertThat(remoteItem.serialNo()).isEqualTo(serialNo);
        assertThat(remoteItem.itemName()).isEqualTo(itemName);
    }

    @Test
    void testFromItem() {
        // given
        Item item = testCreater.newInstance(new ItemId(1), "SN12345", "Laptop");

        // when
        RemoteItem remoteItem = RemoteItem.from(item);

        // then
        assertThat(remoteItem).isNotNull();
        assertThat(remoteItem.id()).isEqualTo(1);
        assertThat(remoteItem.serialNo()).isEqualTo("SN12345");
        assertThat(remoteItem.itemName()).isEqualTo("Laptop");
    }

    @Test
    void testToModel() {
        // given
        RemoteItem remoteItem = new RemoteItem(1, "SN12345", "Laptop");

        // when
        Item iItem = remoteItem.toModel(null);

        // then
        assertThat(iItem).isNotNull();
        assertThat(iItem.getId().id()).isEqualTo(1);
        assertThat(iItem.getSerialNo()).isEqualTo("SN12345");
        assertThat(iItem.getItemName()).isEqualTo("Laptop");
    }
}
