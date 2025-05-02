package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractUserRepositoryTest;

public abstract class AbstractRemoteUserRepositoryTest extends AbstractUserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @BeforeEach
    void beforeEach(@Autowired RemoteRepositoryTestInitializer initializer) {
        initializer.resetAndSignin("SYSTEM");
    }

    @Override
    protected UserRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        int firstTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        new UserId(firstTime),
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        int secondTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        new UserId(secondTime),
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        int thirdTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        new UserId(thirdTime),
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
