package com.aps.env.schedule;

import com.aps.env.communication.NettyClientHandler;
import com.aps.env.communication.NettyServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
        boolean serverStatus = false;
        int port = Integer.parseInt(environment.getProperty("nioTcpPort"));
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        LOG.info("Try to check NIO Server status!");
        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new NettyClientHandler());
                }
            });

            // 启动客户端
            bootstrap.connect("localhost", port).sync();
            serverStatus = true;
            LOG.info("Server's status checked successfully!");
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

        if (!serverStatus) {
            int checkTimes = 0;

            LOG.info("Try to restart NIO Server!");
            NettyServer.restart(port, "Schedule");
            while (!NettyServer.isActivity()) {
                checkTimes++;
                if (checkTimes >= 71) {
                    break;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }

            if (NettyServer.isActivity()) {
                LOG.info("NIO Server has bean restarted!");
            } else {
                LOG.info("Try to start NIO Server failed!");
            }
        }
    }
}
