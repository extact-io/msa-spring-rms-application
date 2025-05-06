package io.extact.msa.spring.rms.infrastructure.persistence.remote.remote;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.AbstractRemoteItemRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryTestInitializer;
import io.extact.msa.spring.test.spring.NopTransactionManager;
import lombok.extern.slf4j.Slf4j;

@RestClientTest
@Testcontainers
@ActiveProfiles({ "test", "item-remote" })
@Slf4j
class RemoteItemRepositoryUsingRemoteStubTest extends AbstractRemoteItemRepositoryTest {

    @Configuration(proxyBeanMethods = false)
    @Import({
            CoreConfig.class,
            RemoteRepositoryConfig.class,
            TestcontainersConfig.class
    })
    static class WebSecurityConfig implements WebMvcConfigurer {
        @Bean
        PlatformTransactionManager nopTransactionManager() {
            return new NopTransactionManager();
        }

        @Bean
        RemoteRepositoryTestInitializer remoteRepositoryTestInitializer(
                @Qualifier("item") ExternalProperties prop,
                Environment env) {
            return new RemoteRepositoryTestInitializer(prop, env, "items");
        }

    }

    @BeforeAll
    static void beforeAll(@Autowired GenericContainer<?> stubContainer) {
        TestcontainersConfig.followOutputContainerLog(stubContainer);
    }
}
