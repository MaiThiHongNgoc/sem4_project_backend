package org.example.sem4backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Sem4BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Sem4BackendApplication.class, args);
    }

}
