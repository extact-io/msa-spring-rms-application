package io.extact.msa.spring.rms.application.admin;

import static io.extact.msa.spring.rms.application.PersistedTestData.*;
import static io.extact.msa.spring.test.assertj.ToStringAssert.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.rms.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.item.ItemCreator;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;

/**
 * テスト観点
 * ・トランザクションが機能しているか
 * ・入出力データクラスの項目マッピング
 * ・domainコンポーネントとの結合
 */
@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class ItemAdminServiceTest {

    private static final ItemCreatable testCreator = new ItemCreatable() {};
    private static final int WITH_SIDE_EFFECT_CASE = 99;

    @Autowired
    private ItemAdminService service;

    @Configuration(proxyBeanMethods = false)
    @Import({ PersistenceConfig.class, DomainConfig.class })
    static class TestConfig {

        @Bean
        ItemAdminService itemAdminService(
                ItemCreator modelCreator,
                DuplicateChecker<Item> duplicateChecker,
                ItemRepository repository) {
            return new ItemAdminService(modelCreator, duplicateChecker, repository);
        }
    }

    @Test
    void testGetAll() {
        // given
        // when
        List<ItemReference> items = service.getAll();

        // then
        assertThatToString(items).containsExactly(item1, item2, item3, item4);
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testAdd(@Autowired ItemRepository forResultAssert) {
        // given
        ItemAddCommand command = ItemAddCommand.builder()
                .serialNo("newNo")
                .itemName("newItem")
                .build();

        // when
        ItemReference actual = service.add(command);

        // then
        Item expected = testCreator.newInstance(new ItemId(1000), "newNo", "newItem");
        assertThatToString(actual).isEqualTo(expected);
        // commitされているかの確認
        Optional<Item> commited = forResultAssert.find(expected.getId());
        assertThat(commited)
                .isPresent()
                .hasValue(expected);
    }

    @Test
    void testAddOnDuplicate(@Autowired ItemRepository forResultAssert) {
        // given
        ItemAddCommand command = ItemAddCommand.builder()
                .serialNo(item1.getSerialNo()) // 重複エラー
                .itemName("newItem")
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.add(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.DUPLICATE);
        // commitされていないことの確認("newItem"にはならずitem1がそのまま)
        Optional<Item> persisted = forResultAssert.findDuplicationData(item1);
        assertThat(persisted)
                .isPresent()
                .hasValue(item1);
    }

    @Test
    void testAddOnValidateionErrorOfProperty(@Autowired ItemRepository forResultAssert) {
        // given
        ItemAddCommand command = ItemAddCommand.builder()
                .serialNo("@+!") // 使用不可文字エラー
                .itemName("Item.newItem")
                .build();

        // when
        RmsValidationException exception = assertThrows(RmsValidationException.class, () -> {
            service.add(command);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(exception)
            .verifyErrorItemFieldOf("Item.serialNo");
        // commitされていないことの確認(4件のまま)
        List<Item> items = forResultAssert.findAll();
        assertThat(items).hasSize(4);
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testUpdate(@Autowired ItemRepository forResultAssert) {
        // given
        ItemUpdateCommand command = ItemUpdateCommand.builder()
                .id(item1.getId())
                .serialNo("upateNo")
                .itemName("upadteItem")
                .build();

        // when
        ItemReference actual = service.update(command);

        // then
        Item expected = testCreator.newInstance(item1.getId(), "upateNo", "upadteItem");
        assertThatToString(actual).isEqualTo(expected);
        // commitされているかの確認
        Optional<Item> commited = forResultAssert.find(expected.getId());
        assertThat(commited)
                .isPresent()
                .hasValue(expected);
    }

    @Test
    void testUpdateOnNotFound(@Autowired ItemRepository forResultAssert) {
        // given
        ItemId notFoundId = new ItemId(99);
        ItemUpdateCommand command = ItemUpdateCommand.builder()
                .id(notFoundId) // 該当なし
                .serialNo("upateNo")
                .itemName("updateItem")
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.update(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
        // 追加されていないことの確認
        Optional<Item> persisted = forResultAssert.find(notFoundId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    void testUpdateOnDuplicate(@Autowired ItemRepository forResultAssert) {
        // given
        ItemUpdateCommand command = ItemUpdateCommand.builder()
                .id(item1.getId())
                .serialNo(item2.getSerialNo()) // 他のItemのシリアルNoなので重複エラー
                .itemName("updateItem")
                .build();

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.update(command);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.DUPLICATE);
        // commitされていないことの確認("updateItem"にはならずitem1がそのまま)
        Optional<Item> persisted = forResultAssert.find(item1.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(item1);
    }

    @Test
    void testUpdateOnValidateionErrorOfProperty(@Autowired ItemRepository forResultAssert) {
        // given
        ItemUpdateCommand command = ItemUpdateCommand.builder()
                .id(item1.getId())
                .serialNo("@+!") // 使用不可文字エラー
                .itemName("newItem")
                .build();

        // when
        RmsValidationException exception = assertThrows(RmsValidationException.class, () -> {
            service.update(command);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(exception)
            .verifyErrorItemFieldOf("Item.serialNo");
        // commitされていないことの確認("updateItem"にはならずitem1がそのまま)
        Optional<Item> persisted = forResultAssert.find(item1.getId());
        assertThat(persisted)
                .isPresent()
                .hasValue(item1);
    }

    @Test
    @Order(WITH_SIDE_EFFECT_CASE)
    void testDelete(@Autowired ItemRepository forResultAssert) {
        // given
        ItemId deleteId = item4.getId();

        // when
        service.delete(deleteId);

        // then
        Optional<Item> persisted = forResultAssert.find(deleteId);
        assertThat(persisted).isNotPresent();
    }

    @Test
    void testDeleteOnNotFound(@Autowired ItemRepository forResultAssert) {
        // given
        ItemId notFoundId = new ItemId(99);

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.delete(notFoundId);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }
}
