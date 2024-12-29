package io.extact.msa.spring.rms.infrastructure.item.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.DefaultModelEntityMapper;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = ItemEntity.class)
@EnableJpaRepositories(basePackageClasses = ItemJpaRepositoryDelegator.class)
@Import(ValidationConfiguration.class)
@Profile("jpa")
class ItemJpaRepositoryConfig {

    @Bean
    ItemJpaRepository itemJpaRepository(ItemJpaRepositoryDelegator delegator) {
        return new ItemJpaRepository(
                delegator,
                new DefaultModelEntityMapper<>(ItemEntity::from));
    }
}
