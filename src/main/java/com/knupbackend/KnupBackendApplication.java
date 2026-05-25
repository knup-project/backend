package com.knupbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KnupBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnupBackendApplication.class, args);
    }

}
