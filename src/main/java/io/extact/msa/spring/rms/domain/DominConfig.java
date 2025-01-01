package io.extact.msa.spring.rms.domain;

import jakarta.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.rms.domain.item.ItemCreator;
import io.extact.msa.spring.rms.domain.item.ItemRepository;

@Configuration(proxyBeanMethods = false)
@Import({
    ValidationConfiguration.class
})
public class DominConfig {

    @Bean
    ItemCreator itemCreator(
                ItemRepository repository,
                Validator validator) {
        return new ItemCreator(repository, validator);
    }
}
