package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static io.extact.msa.spring.platform.fw.feature.profile.PersistenceProfileType.*;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.feature.profile.ConditionalOnAnyPersistenceProfile;
import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.platform.fw.infrastructure.datasource.ApplicationDataSourceConfig;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.DefaultModelEntityMapper;
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
        ValidatorConfig.class,
        ApplicationDataSourceConfig.class })
public class JpaRepositoryConfig {

    @Configuration(proxyBeanMethods = false)
    @Profile("item-jpa")
    @EntityScan(basePackageClasses = ItemEntity.class)
    @EnableJpaRepositories(basePackageClasses = ItemJpaRepositoryDelegator.class)
    static class ItemJpaConfiguration {
        @Bean
        ItemJpaRepository itemJpaRepository(ItemJpaRepositoryDelegator delegator, ModelValidator validator) {
            return new ItemJpaRepository(
                    delegator,
                    new DefaultModelEntityMapper<>(ItemEntity::from, validator));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("reservation-jpa")
    @EntityScan(basePackageClasses = ReservationEntity.class)
    @EnableJpaRepositories(basePackageClasses = ReservationJpaRepository.class)
    static class ReservationJpaConfiguration {
        @Bean
        ReservationJpaRepository reservationJpaRepository(
                ReservationJpaRepositoryDelegator delegator,
                ModelValidator validator) {
            return new ReservationJpaRepository(
                    delegator,
                    new DefaultModelEntityMapper<>(ReservationEntity::from, validator));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("user-jpa")
    @EntityScan(basePackageClasses = UserEntity.class)
    @EnableJpaRepositories(basePackageClasses = UserJpaRepository.class)
    static class UserJpaConfiguration {
        @Bean
        UserJpaRepository userJpaRepository(UserJpaRepositoryDelegator delegator, ModelValidator validator) {
            return new UserJpaRepository(
                    delegator,
                    new DefaultModelEntityMapper<>(UserEntity::from, validator));
        }
    }
}
