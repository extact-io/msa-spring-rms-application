package io.extact.msa.spring.rms.application.universal;

import static io.extact.msa.spring.rms.test.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.UserModelView;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;
import io.extact.msa.spring.test.assertj.ToStringAssert;

@DataJpaTest
@ActiveProfiles({ "test", "jpa-all" })
class LoginServiceTest {

    @Autowired
    private LoginService service;

    @Configuration(proxyBeanMethods = false)
    @Import({
            PersistenceConfig.class,
            DomainConfig.class })
    static class TestConfig {
        @Bean
        LoginService loginService(UserRepository repository) {
            return new LoginService(repository);
        }
    }

    @Test
    void testLoginOK() {
        // given
        String loginId = "member1";
        String password = "member1";

        // when
        UserModelView user = service.login(loginId, password);

        // then
        ToStringAssert.assertThatToString(user).isEqualTo(user1);
    }

    @Test
    void testLoginNG() {
        // given
        String loginId = "incorrect";
        String password = "incorrect";

        // when
        BusinessFlowException exception = assertThrows(BusinessFlowException.class, () -> {
            service.login(loginId, password);
        });

        // then
        assertThat(exception.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }
}
