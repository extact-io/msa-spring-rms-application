package io.extact.msa.spring.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("console")
public class ConsoleApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
