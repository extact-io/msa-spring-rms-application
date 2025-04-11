package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

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
import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractReservationRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.stub.RemoteReservationStubController;
import io.extact.msa.spring.test.spring.NopTransactionManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "test", "reservation-remote" })
class RemoteReservationRepositoryTest extends AbstractReservationRepositoryTest {
    
    @Autowired
    private ReservationRepository repository;
    @Autowired
    private ReserveItemQueryService queryService;

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
        RemoteReservationStubController remoteReservationStubController() {
            return new RemoteReservationStubController();
        }
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
    
    @BeforeEach
    void beforeEach(@Autowired RemoteRepositoryTestInitializer initializer) {
        initializer.resetAndSignin();
    }
    
    @Override
    protected ReservationRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        int firstTime = repository.nextIdentity();
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = from.plusDays(1);
        repository.add(testCreator.newInstance(
                new ReservationId(firstTime),
                new ReservationPeriod(from, to),
                "1st",
                new ItemId(1),
                new UserId(1)));

        int secondTime = repository.nextIdentity();
        from = from.plusDays(1);
        to = to.plusDays(1);
        repository.add(testCreator.newInstance(
                new ReservationId(secondTime),
                new ReservationPeriod(from, to),
                "2nd",
                new ItemId(1),
                new UserId(1)));

        int thirdTime = repository.nextIdentity();
        from = from.plusDays(1);
        to = to.plusDays(1);
        repository.add(testCreator.newInstance(
                new ReservationId(thirdTime),
                new ReservationPeriod(from, to),
                "3rd",
                new ItemId(1),
                new UserId(1)));

        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }

    @Override
    protected ReserveItemQueryService queryService() {
        return this.queryService;
    }
}
