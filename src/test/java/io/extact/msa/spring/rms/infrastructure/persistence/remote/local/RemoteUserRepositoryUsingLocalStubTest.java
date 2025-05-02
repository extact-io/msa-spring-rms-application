package io.extact.msa.spring.rms.infrastructure.persistence.remote.local;

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

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.platform.core.auth.configure.AuthorizeHttpRequestCustomizer;
import io.extact.msa.spring.platform.core.auth.header.RmsHeaderAuthConfig;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.AbstractRemoteUserRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryTestInitializer;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.local.stub.RemoteUserStubController;
import io.extact.msa.spring.test.spring.NopTransactionManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "user-remote" })
class RemoteUserRepositoryUsingLocalStubTest extends AbstractRemoteUserRepositoryTest {

    @Configuration(proxyBeanMethods = false)
    @Import({
            CoreConfig.class,
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
}
