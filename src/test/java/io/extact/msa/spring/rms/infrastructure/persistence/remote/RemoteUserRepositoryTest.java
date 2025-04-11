package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.extact.msa.spring.platform.core.auth.configure.AuthorizeHttpRequestCustomizer;
import io.extact.msa.spring.platform.core.auth.header.RmsHeaderAuthConfig;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.core.log.LogConfig;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractUserRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.stub.RemoteUserStubController;
import io.extact.msa.spring.test.spring.NopTransactionManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "user-remote" })
class RemoteUserRepositoryTest extends AbstractUserRepositoryTest {
    
    @Autowired
    private UserRepository repository;

    @Configuration(proxyBeanMethods = false)
    @Import({
            LogConfig.class,
            EnvConfig.class,
            RestControllerConfig.class,
            RmsHeaderAuthConfig.class,
            RemoteRepositoryConfig.class
    })
    static class WebSecurityConfig implements WebMvcConfigurer {
        @Bean
        AuthorizeHttpRequestCustomizer authorizeRequestCustomizer() {
            return (AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry configurer) -> configurer
                    .anyRequest().authenticated();
        }
        @Bean
        RemoteUserStubController remoteUserStubController() {
            return new RemoteUserStubController();
        }
        @Bean
        PlatformTransactionManager nopTransactionManager() {
            return new NopTransactionManager();
        }
        @Bean
        RemoteRepositoryTestInitializer remoteRepositoryTestInitializer(
                @Qualifier("user") ExternalProperties prop,
                Environment env) {
            return new RemoteRepositoryTestInitializer(prop, env, "users");
        }
    }
    
    @BeforeEach
    void beforeEach(@Autowired RemoteRepositoryTestInitializer initializer) {
        initializer.resetAndSignin();
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
