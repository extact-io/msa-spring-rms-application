package io.extact.msa.spring.rms.application.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.rms.application.infrastructure.persistence.AbstractUserRepositoryTest;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.JpaRepositoryConfig;

@DataJpaTest
@ActiveProfiles("user-jpa")
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
        int firstTime = repository.nextIdentity();
        int secondTime = repository.nextIdentity();
        int thirdTime = repository.nextIdentity();

        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
