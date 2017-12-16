package com.aps.env.service;

import com.aps.env.communication.NioServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <dl>
 * <dt>com.aps.env.service.StartNioServer</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    AppleShow Technology </dd>
 * <dd>CreateDate: 2017/12/13</dd>
 * </dl>
 *
 * @author appleshow
 */
@Component
public class StartNioServer implements CommandLineRunner {
    @Autowired
    private Environment environment;
    @Resource
    private InitDataService initDataService;

    @Override
    public void run(String... strings) throws Exception {
        initDataService.initHbNode();

        NioServer.start(Integer.parseInt(environment.getProperty("nioTcpPort")));
    }
}
