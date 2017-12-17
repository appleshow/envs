package com.aps.env;

import com.aps.env.communication.NioServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableScheduling
@CrossOrigin(origins = "*")
@MapperScan(value = "com.aps.env.dao")
public class EnvsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnvsApplication.class, args);
    }

    @RequestMapping(value = "getOnlineClient", method = RequestMethod.GET)
    public List<String> getOnlineClient() {
        final List<String> onlineClient = new ArrayList<>();

        NioServer.getManagedSessions().forEach((id, session) -> onlineClient.add("Host: " + session.getRemoteAddress()));

        return onlineClient;
    }

}
