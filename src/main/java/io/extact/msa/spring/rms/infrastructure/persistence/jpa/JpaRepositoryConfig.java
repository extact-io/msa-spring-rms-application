package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static io.extact.msa.spring.platform.fw.infrastructure.framework.profile.PersistenceProfileType.*;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfig;
import io.extact.msa.spring.platform.fw.infrastructure.framework.profile.ConditionalOnAnyPersistenceProfile;
import io.extact.msa.spring.platform.fw.infrastructure.framework.sqlinit.ProfileBasedDbInitializerConfig;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.DefaultModelEntityMapper;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.item.ItemEntity;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.item.ItemJpaRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.item.ItemJpaRepositoryDelegator;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation.ReservationEntity;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation.ReservationJpaRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation.ReservationJpaRepositoryDelegator;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.user.UserEntity;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.user.UserJpaRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.user.UserJpaRepositoryDelegator;

@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyPersistenceProfile(JPA)
@Import({
        ValidationConfig.class,
        ProfileBasedDbInitializerConfig.class })
public class JpaRepositoryConfig {

    @Configuration(proxyBeanMethods = false)
    @Profile("item-jpa")
    @EntityScan(basePackageClasses = ItemEntity.class)
    @EnableJpaRepositories(basePackageClasses = ItemJpaRepositoryDelegator.class)
    static class ItemFileConfiguration {
        @Bean
        ItemJpaRepository itemJpaRepository(ItemJpaRepositoryDelegator delegator) {
            return new ItemJpaRepository(
                    delegator,
                    new DefaultModelEntityMapper<>(ItemEntity::from));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("reservation-jpa")
    @EntityScan(basePackageClasses = ReservationEntity.class)
    @EnableJpaRepositories(basePackageClasses = ReservationJpaRepository.class)
    static class ReservationFileConfiguration {
        @Bean
        ReservationJpaRepository reservationJpaRepository(ReservationJpaRepositoryDelegator delegator) {
            return new ReservationJpaRepository(
                    delegator,
                    new DefaultModelEntityMapper<>(ReservationEntity::from));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("user-jpa")
    @EntityScan(basePackageClasses = UserEntity.class)
    @EnableJpaRepositories(basePackageClasses = UserJpaRepository.class)
    static class UserFileConfiguration {
        @Bean
        UserJpaRepository userJpaRepository(UserJpaRepositoryDelegator delegator) {
            return new UserJpaRepository(
                    delegator,
                    new DefaultModelEntityMapper<>(UserEntity::from));
        }
    }
}
