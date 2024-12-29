package io.extact.msa.spring.rms.infrastructure.reservation.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.DefaultModelEntityMapper;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = ReservationEntity.class)
@EnableJpaRepositories(basePackageClasses = ReservationJpaRepositoryDelegator.class)
@Import(ValidationConfiguration.class)
@Profile("jpa")
class ReservationJpaRepositoryConfig {

    @Bean
    ReservationJpaRepository userJpaRepository(ReservationJpaRepositoryDelegator delegator) {
        return new ReservationJpaRepository(
                delegator,
                new DefaultModelEntityMapper<>(ReservationEntity::from));
    }
}
