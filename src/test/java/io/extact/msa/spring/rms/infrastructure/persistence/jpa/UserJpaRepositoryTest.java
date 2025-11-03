package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractUserRepositoryTest;

@DataJpaTest
@ActiveProfiles({ "test", "user-jpa" })
class UserJpaRepositoryTest extends AbstractUserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Configuration(proxyBeanMethods = false)
    @Import(JpaRepositoryConfig.class)
    static class TestConfig {
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
        UserId secondTime = repository.nextIdentity();
        UserId thirdTime = repository.nextIdentity();

        // then
        assertThat(secondTime.id()).isEqualTo(firstTime.id() + 1);
        assertThat(thirdTime.id()).isEqualTo(secondTime.id() + 1);
    }
}
