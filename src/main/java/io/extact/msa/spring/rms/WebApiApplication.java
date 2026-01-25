package io.extact.msa.spring.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.platform.core.auth.context.LoginContextConfig;
import io.extact.msa.spring.platform.fw.feature.observation.ObservationConfig;
import io.extact.msa.spring.rms.application.ApplicationServiceConfig;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.infrastructure.framework.FrameworkConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;
import io.extact.msa.spring.rms.webapi.WebApiConfig;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
        CoreConfig.class,
        ObservationConfig.class,
        LoginContextConfig.class,
        WebApiConfig.class,
        ApplicationServiceConfig.class,
        DomainConfig.class,
        PersistenceConfig.class,
        FrameworkConfig.class })
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
