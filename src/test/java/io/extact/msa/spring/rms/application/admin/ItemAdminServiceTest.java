package io.extact.msa.spring.rms.application.admin;

import static io.extact.msa.spring.test.assertj.ToStringAssert.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.item.ItemCreator;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.infrastructure.persistence.file.FileRepositoryConfig;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles({
        "item-file",
        "reservation-file",
        "user-file" })
class ItemAdminServiceTest {

    protected static final ItemCreatable testCreator = new ItemCreatable() {};

    private static final Item item1 = testCreator.newInstance(new ItemId(1), "A0001", "レンタル品1号");
    private static final Item item2 = testCreator.newInstance(new ItemId(2), "A0002", "レンタル品2号");
    private static final Item item3 = testCreator.newInstance(new ItemId(3), "A0003", "レンタル品3号");
    private static final Item item4 = testCreator.newInstance(new ItemId(4), "A0004", "レンタル品4号");

    @Autowired
    private ItemAdminService service;

    @Configuration(proxyBeanMethods = false)
    @Import({ FileRepositoryConfig.class, DomainConfig.class })
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
        // when
        assertThatToString(items).containsExactlyElementsOf(items);
    }

    @Test
    void testAdd() {
        // given
        ItemAddCommand command = ItemAddCommand.builder()
                .serialNo("newNo")
                .itemName("newItem")
                .build();
        // when
        ItemReference item = service.add(command);
        // when
        assertThatToString(items).isEqualTo();
    }
}
