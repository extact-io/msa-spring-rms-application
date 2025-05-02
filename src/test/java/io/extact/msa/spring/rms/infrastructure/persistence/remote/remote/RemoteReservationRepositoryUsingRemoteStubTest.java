package io.extact.msa.spring.rms.infrastructure.persistence.remote.remote;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.AbstractRemoteReservationRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryTestInitializer;
import io.extact.msa.spring.test.spring.NopTransactionManager;

@RestClientTest
@TestPropertySource(properties = "rms.persistence.reservation.remote.url=http://localhost:8081/remote")
@ActiveProfiles({ "test", "reservation-remote" })
class RemoteReservationRepositoryUsingRemoteStubTest extends AbstractRemoteReservationRepositoryTest {

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
                @Qualifier("reservation") ExternalProperties prop,
                Environment env) {
            return new RemoteRepositoryTestInitializer(prop, env, "reservations");
        }
    }
}
