package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static io.extact.msa.spring.platform.fw.feature.profile.PersistenceProfileType.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.feature.profile.ConditionalOnAnyPersistenceProfile;
import io.extact.msa.spring.platform.fw.feature.sqlinit.ProfileBasedDbInitializerConfig;
import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.DefaultModelEntityMapper;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItem;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItemClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItemRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservation;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservationClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservationRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUser;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUserClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUserRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyPersistenceProfile(REMOTE)
@Import({
        ValidatorConfig.class,
        ProfileBasedDbInitializerConfig.class })
public class RemoteRepositoryConfig {

    @Configuration(proxyBeanMethods = false)
    @Profile("item-remote")
    class ItemRemoteConfiguration {
        @Bean
        RemoteItemRepository itemJpaRepository(RemoteItemClientApi client, ModelValidator validator) {
            return new RemoteItemRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteItem::from, validator));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("reservation-remote")
    class ReservationRemoteConfiguration {
        @Bean
        RemoteReservationRepository remoteReservationRepository(RemoteReservationClientApi client, ModelValidator validator) {
            return new RemoteReservationRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteReservation::from, validator));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("user-remote")
    class UserRemoteConfiguration {
        @Bean
        RemoteUserRepository remoteUserRepository(RemoteUserClientApi client, ModelValidator validator) {
            return new RemoteUserRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteUser::from, validator));
        }
    }
}
