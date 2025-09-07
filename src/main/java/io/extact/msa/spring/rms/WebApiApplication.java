package io.extact.msa.spring.rms;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.platform.core.auth.context.LoginContextConfig;
import io.extact.msa.spring.rms.application.ApplicationServiceConfig;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;
import io.extact.msa.spring.rms.webapi.WebApiConfig;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

@SpringBootConfiguration
@Import({
        CoreConfig.class,
        LoginContextConfig.class,
        WebApiConfig.class,
        ApplicationServiceConfig.class,
        DomainConfig.class,
        PersistenceConfig.class })
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }

    // TODO: Condationalにしてfwに持っていく
    @Bean
    OpenTelemetryAppenderInitializer openTelemetryAppenderInitializer(OpenTelemetry openTelemetry) {
        return new OpenTelemetryAppenderInitializer(openTelemetry);
    }

    static class OpenTelemetryAppenderInitializer {

        private final OpenTelemetry openTelemetry;

        OpenTelemetryAppenderInitializer(OpenTelemetry openTelemetry) {
            this.openTelemetry = openTelemetry;
        }

        @PostConstruct
        void init() {
            OpenTelemetryAppender.install(this.openTelemetry);
        }

    }
}
