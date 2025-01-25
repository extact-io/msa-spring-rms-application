package io.extact.msa.spring.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.rms.application.ApplicationServiceConfig;
import io.extact.msa.spring.rms.boundary.webapi.WebApiConfig;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
        CoreConfig.class,
        ApplicationServiceConfig.class,
        DomainConfig.class,
        PersistenceConfig.class,
        WebApiConfig.class })
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
