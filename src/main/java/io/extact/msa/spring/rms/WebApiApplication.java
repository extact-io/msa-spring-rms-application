package io.extact.msa.spring.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import io.extact.msa.spring.platform.core.CoreConfig;

@SpringBootApplication
@Import(CoreConfig.class)
@Profile("webapi")
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
