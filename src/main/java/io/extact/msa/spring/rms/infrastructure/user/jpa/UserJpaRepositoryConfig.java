package io.extact.msa.spring.rms.infrastructure.user.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.DefaultModelEntityMapper;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = UserEntity.class)
@EnableJpaRepositories(basePackageClasses = UserJpaRepositoryDelegator.class)
@Import(ValidationConfiguration.class)
@Profile("jpa")
class UserJpaRepositoryConfig {

    @Bean
    UserJpaRepository userJpaRepository(UserJpaRepositoryDelegator delegator) {
        return new UserJpaRepository(
                delegator,
                new DefaultModelEntityMapper<>(UserEntity::from));
    }
}
