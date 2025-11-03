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
        UserId firstTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        firstTime,
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        UserId secondTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        secondTime,
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        UserId thirdTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        thirdTime,
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        // then
        assertThat(secondTime.id()).isEqualTo(firstTime.id() + 1);
        assertThat(thirdTime.id()).isEqualTo(secondTime.id() + 1);
    }
}
