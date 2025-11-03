package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractItemRepositoryTest;

public abstract class AbstractRemoteItemRepositoryTest extends AbstractItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @BeforeEach
    void beforeEach(@Autowired RemoteRepositoryTestInitializer initializer) throws InterruptedException {
        //Thread.sleep(1000);
        initializer.resetAndSignin("SYSTEM");
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
        repository.add(testCreator.newInstance(firstTime, "1st", ""));
        ItemId secondTime = repository.nextIdentity();
        repository.add(testCreator.newInstance(secondTime, "2nd", ""));
        ItemId thirdTime = repository.nextIdentity();
        repository.add(testCreator.newInstance(thirdTime, "3rd", ""));

        // then
        assertThat(secondTime.id()).isEqualTo(firstTime.id() + 1);
        assertThat(thirdTime.id()).isEqualTo(secondTime.id() + 1);
    }
}
