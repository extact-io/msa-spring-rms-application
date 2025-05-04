package io.extact.msa.spring.rms.infrastructure.persistence.remote.remote;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.AbstractRemoteItemRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryTestInitializer;
import io.extact.msa.spring.test.spring.NopTransactionManager;

@RestClientTest
@Testcontainers
//@TestPropertySource(properties = "rms.persistence.item.remote.url=http://localhost:8081/remote")
@ActiveProfiles({ "test", "item-remote" })
class RemoteItemRepositoryUsingRemoteStubTest extends AbstractRemoteItemRepositoryTest {

    @Container
    static GenericContainer<?> remoteStub = new GenericContainer<>("msa-spring-rms-remote-resources:0.0.1-SNAPSHOT")
            .withExposedPorts(8081);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String destination = "http://" + remoteStub.getHost() + ":" + remoteStub.getFirstMappedPort();
        registry.add("rms.persistence.item.remote.url", () -> destination);
    }

    @Configuration(proxyBeanMethods = false)
    @Import({
            CoreConfig.class,
            RemoteRepositoryConfig.class
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
            return new RemoteRepositoryTestInitializer(prop, env, "remote/items");
        }
    }
}
