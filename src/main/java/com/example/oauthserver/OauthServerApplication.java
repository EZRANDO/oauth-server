package com.example.oauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class OauthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthServerApplication.class, args);
    }

}
