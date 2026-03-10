package com.itsm.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.itsm.batch", "com.itsm.core"})
@EntityScan(basePackages = "com.itsm.core")
@EnableJpaRepositories(basePackages = "com.itsm.core")
public class ItsmBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItsmBatchApplication.class, args);
    }
}
