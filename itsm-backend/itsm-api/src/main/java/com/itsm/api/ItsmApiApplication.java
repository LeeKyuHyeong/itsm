package com.itsm.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.itsm.api", "com.itsm.core"})
@EntityScan(basePackages = "com.itsm.core")
@EnableJpaRepositories(basePackages = "com.itsm.core")
public class ItsmApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItsmApiApplication.class, args);
    }
}
