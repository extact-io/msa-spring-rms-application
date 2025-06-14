package io.extact.msa.spring.rms;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.rms.application.ApplicationServiceConfig;
import io.extact.msa.spring.rms.domain.DomainConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.PersistenceConfig;
import io.extact.msa.spring.rms.interfaces.console.ConsoleConfig;

//localの場合はwebapiの@SpringBootConfigurationがクラスパスに含まれるためConfigurationを使っている
@Configuration
@EnableAutoConfiguration
@Import({
        CoreConfig.class,
        ApplicationServiceConfig.class,
        DomainConfig.class,
        PersistenceConfig.class,
        ConsoleConfig.class })
public class ConsoleApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder()
                .sources(ConsoleApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
