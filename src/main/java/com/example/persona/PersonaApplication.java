package com.example.persona;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(
        scanBasePackages = "com.example.persona",
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PersonaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonaApplication.class, args);
    }
}
