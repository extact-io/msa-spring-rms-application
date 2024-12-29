package io.extact.msa.spring.rms.domain.item;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.domain.service.SimpleDuplicateChecker;
import io.extact.msa.spring.rms.domain.item.model.Item;

@Configuration(proxyBeanMethods = false)
class ItemDomainConfig {

    @Bean
    DuplicateChecker<Item> duplicateChecker(ItemRepository repository) {
        return new SimpleDuplicateChecker<Item>(repository);
    }
}
