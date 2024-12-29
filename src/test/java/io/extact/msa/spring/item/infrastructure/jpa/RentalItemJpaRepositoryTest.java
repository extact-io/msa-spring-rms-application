package io.extact.msa.spring.item.infrastructure.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.item.domain.RentalItemRepository;
import io.extact.msa.spring.item.infrastructure.RentalItemRepositoryTest;

@DataJpaTest
@ActiveProfiles("jpa")
class RentalItemJpaRepositoryTest extends RentalItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Configuration(proxyBeanMethods = false)
    @Import(ItemJpaRepositoryConfig.class)
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
        int firstTime = repository.nextIdentity();
        int secondTime = repository.nextIdentity();
        int thirdTime = repository.nextIdentity();
        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
