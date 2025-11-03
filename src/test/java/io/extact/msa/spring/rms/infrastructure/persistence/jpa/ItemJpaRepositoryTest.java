package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractItemRepositoryTest;

@DataJpaTest
@ActiveProfiles({ "test", "item-jpa" })
class ItemJpaRepositoryTest extends AbstractItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Configuration(proxyBeanMethods = false)
    @Import(JpaRepositoryConfig.class)
    static class TestConfig {
    }

    @Override
    protected ItemRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        ItemId firstTime = repository.nextIdentity();
        ItemId secondTime = repository.nextIdentity();
        ItemId thirdTime = repository.nextIdentity();

        // then
        assertThat(secondTime.id()).isEqualTo(firstTime.id() + 1);
        assertThat(thirdTime.id()).isEqualTo(secondTime.id() + 1);
    }
}
