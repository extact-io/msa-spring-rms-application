package io.extact.msa.spring.rms.domain.item;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.repository.IdProvider;
import io.extact.msa.spring.platform.fw.feature.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.platform.fw.test.utils.RmsValidationExceptionAsserter;
import io.extact.msa.spring.rms.domain.InMemoryIdentityGenerator;
import io.extact.msa.spring.rms.domain.item.ItemCreator.ItemModelAttributes;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class ItemCreatorTest {

    private ItemCreator itemCreator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
        @Bean
        @Scope("prototype")
        IdProvider<ItemId> identityGenerator() {
            return new InMemoryIdentityGenerator<>(ItemId::new);
        }
    }

    @BeforeEach
    void beforeEach(@Autowired IdProvider<ItemId> idProvider, @Autowired ModelValidator validator) {
        this.itemCreator = new ItemCreator(idProvider, validator);
    }

    @Test
    void testCreateModel() {

        // given
        ItemModelAttributes attrs = ItemModelAttributes.builder()
                .serialNo("serialNo")
                .itemName("itemName")
                .build();

        // when
        Item item = itemCreator.create(attrs);

        // then
        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo(new ItemId(1));
        assertThat(item.getSerialNo()).isEqualTo("serialNo");
        assertThat(item.getItemName()).isEqualTo("itemName");
    }

    @Test
    void testCreateModelOnValidationError() {

        // given
        ItemModelAttributes attrs = ItemModelAttributes.builder()
                .serialNo("シリアル番号") // 文字種エラー
                .itemName("itemName")
                .build();

        // when
        RmsValidationException e = assertThrows(RmsValidationException.class, () -> {
            itemCreator.create(attrs);
        });

        // then
        RmsValidationExceptionAsserter.asserterTo(e)
                .verifyMessageHeader()
                .verifyErrorItemFieldOf("Item.serialNo");
    }
}
