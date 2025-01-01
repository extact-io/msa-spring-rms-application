package io.extact.msa.spring.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.CoreConfig;
import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.platform.fw.StartupLog;

@SpringBootApplication
@Import(CoreConfig.class)
public class WebApiApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(WebApiApplication.class, args);
        StartupLog.startupLog(context.getBean(MainModuleInformation.class));
    }
}
