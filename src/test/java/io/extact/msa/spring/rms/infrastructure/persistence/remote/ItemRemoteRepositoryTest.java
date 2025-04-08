package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.extact.msa.spring.platform.core.auth.configure.AuthorizeHttpRequestCustomizer;
import io.extact.msa.spring.platform.core.auth.header.RmsHeaderAuthConfig;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractItemRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.stub.RemoteItemStubController;
import io.extact.msa.spring.test.spring.NopTransactionManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "item-remote" })
class ItemRemoteRepositoryTest extends AbstractItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            RmsHeaderAuthConfig.class,
            RemoteRepositoryConfig.class
    })
    static class WebSecurityConfig implements WebMvcConfigurer {
        @Bean
        AuthorizeHttpRequestCustomizer authorizeRequestCustomizer() {
            return (AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry configurer) -> configurer
                    .anyRequest().permitAll();
        }
        @Bean
        RemoteItemStubController remoteItemStubController() {
            return new RemoteItemStubController();
        }
        @Bean
        PlatformTransactionManager nopTransactionManager() {
            return new NopTransactionManager();
        }
    }
    
    @Override
    protected ItemRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        int firstTime = repository.nextIdentity();
        repository.add(testCreator.newInstance(new ItemId(firstTime), "1st", ""));
        int secondTime = repository.nextIdentity();
        repository.add(testCreator.newInstance(new ItemId(secondTime), "2nd", ""));
        int thirdTime = repository.nextIdentity();
        repository.add(testCreator.newInstance(new ItemId(thirdTime), "3rd", ""));

        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
