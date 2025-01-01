package io.extact.msa.spring.rms.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.extact.msa.spring.rms.application.admin.ItemAdminService;

@Configuration(proxyBeanMethods = false)
public class ApplicationServiceConfig {

    @Bean
    ItemAdminService itemAdminService() {
        return new ItemAdminService(null, null, null);
    }

}
