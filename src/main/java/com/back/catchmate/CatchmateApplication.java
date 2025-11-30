package com.back.catchmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CatchmateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatchmateApplication.class, args);
    }

}
