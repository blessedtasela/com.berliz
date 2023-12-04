package com.berliz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@PropertySource("file:${user.dir}/.env")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
