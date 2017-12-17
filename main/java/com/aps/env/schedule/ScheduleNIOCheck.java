package com.aps.env.schedule;

import com.aps.env.communication.NioClient;
import com.aps.env.communication.NioServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <dl>
 * <dt>com.aps.env.schedule.SchedulePullData</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    AppleShow Technology </dd>
 * <dd>CreateDate: 2017/12/14</dd>
 * </dl>
 *
 * @author appleshow
 */
@Component
public class ScheduleNIOCheck {
    @Autowired
    private Environment environment;
    private static final Logger LOG = LogManager.getLogger(ScheduleNIOCheck.class);

    /**
     * 检查NIO状态
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void nioStatus() {
        NioClient nioClient = new NioClient(Integer.parseInt(environment.getProperty("nioTcpPort")));
        if (!nioClient.tryToConnectServer()) {
            LOG.info("Try to connect server failed! Restart server ...");
            NioServer.stop();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
            NioServer.start(Integer.parseInt(environment.getProperty("nioTcpPort")));
            LOG.info("Server has been restated!");
        } else {
            LOG.info("Server's status checked successfully!");
        }
    }
}
