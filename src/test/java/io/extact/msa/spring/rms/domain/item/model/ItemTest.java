package io.extact.msa.spring.rms.domain.item.model;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.framework.model.DefaultModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.ConstraintAnnotationAsserter;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.item.constraint.ItemName;
import io.extact.msa.spring.rms.domain.item.constraint.SerialNo;

/**
 * Itemクラスのテスト。
 * 以下の観点はItemCreatorTestで実施されているためモデルクラスとしてのテストは不要。
 * ・生成されたインスタンスの各プロパティが正しく設定されていること
 * ・{@code Verifiable#verify()}に対するテスト（正常とエラーの両パターン）
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ItemTest {

    @Autowired
    private ModelValidator modelValidator;
    @Autowired
    private Validator beanValidator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testApplyAnnotationCorrectly() {

        BeanDescriptor bd = beanValidator.getConstraintsForClass(ItemId.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", RmsId.class);

        bd = beanValidator.getConstraintsForClass(Item.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", NotNull.class)
                .verifyPropertyAnnotations("serialNo", SerialNo.class)
                .verifyPropertyAnnotations("itemName", ItemName.class);
    }

    @Test
    void testEditItemOK() {
        // geven
        Item item = newItem();
        ItemId oldId = item.getId();
        String newSerialNo = "newNo";
        String newItemName = "newName";

        // when
        assertThatCode(() -> {
            item.editItem(newSerialNo, newItemName);
        })
        // then
        .doesNotThrowAnyException();
        // 変更箇所が反映されていること
        assertThat(item.getSerialNo()).isEqualTo(newSerialNo);
        assertThat(item.getItemName()).isEqualTo(newItemName);
        // 関係ない個所に副作用が発生していないこと
        assertThat(item.getId()).isEqualTo(oldId);
    }

    @Test
    void testEditItemNG() {
        // geven
        Item item = newItem();

        String oldSerialNo = item.getSerialNo();
        String oldItemName = item.getItemName();

        String newSerialNo = "@*?"; // 文字種エラー
        String newItemName = "1234567890123456"; // 文字数オーバー

        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            item.editItem(newSerialNo, newItemName);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf("Item.serialNo"); // 最初に検知の1つだけ

        // モデルが更新されてないこと
        assertThat(item.getSerialNo()).isEqualTo(oldSerialNo);
        assertThat(item.getItemName()).isEqualTo(oldItemName);
    }

    private Item newItem() {
        Item item = new Item(new ItemId(1), "serialNo", "itemName");
        item.configureSupport(new DefaultModelPropertySupportFactory(modelValidator));
        return item;
    }
}
