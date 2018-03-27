package com.aps.env;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableScheduling
@CrossOrigin(origins = "*")
@MapperScan(value = "com.aps.env.dao")
public class EnvsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnvsApplication.class, args);
    }

}
