package io.extact.msa.spring.rms.infrastructure.item.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.LoadPathDeriver;
import io.extact.msa.spring.rms.domain.item.model.Item;

@Configuration(proxyBeanMethods = false)
@Import(ValidationConfiguration.class)
@Profile("file")
class RentalItemFileRepositoryConfig {

    @Bean
    ModelArrayMapper<Item> rentalItemArrayMapper() {
        return ItemArrayMapper.INSTANCE;
    }

    @Bean
    ItemFileRepository itemFileRepository(Environment env, ModelArrayMapper<Item> mapper) {
        LoadPathDeriver pathDeriver = new LoadPathDeriver(env);
        FileOperator fileOperator = new FileOperator(pathDeriver.derive(ItemFileRepository.FILE_ENTITY));
        return new ItemFileRepository(fileOperator, mapper);
    }
}
