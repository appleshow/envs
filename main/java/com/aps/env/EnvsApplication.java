package com.aps.env;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableScheduling
@MapperScan(value = "com.aps.env.dao")
public class EnvsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnvsApplication.class, args);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "Hello world!";
    }

}
