package com.back.catchmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CatchmateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatchmateApplication.class, args);
    }

}
