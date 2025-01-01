package io.extact.msa.spring.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsoleApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
