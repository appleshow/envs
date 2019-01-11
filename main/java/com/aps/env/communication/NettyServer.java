package com.aps.env.communication;

import com.aps.env.comm.DateUtil;
import com.aps.env.entity.ManagedConnection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <dl>
 * <dt>com.aps.env.communication.NettyServer</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    DXC Technology </dd>
 * <dd>CreateDate: 2018/3/20</dd>
 * </dl>
 *
 * @author appleshow
 */
public class NettyServer {
    private static final Logger LOG = LogManager.getLogger(NettyServer.class);

    private static EventLoopGroup eventLoopGroupBoss;
    private static EventLoopGroup eventLoopGroupWorker;
    private static ChannelFuture channelFuture;


    private static boolean activity = false;
    private static int restartPort = -1;
    private static String restartFrom = null;
    private static AtomicInteger connectNumber;
    private static final Vector<String> SERVER_START_TIME;
    private static final ConcurrentHashMap<String, ManagedConnection> MANAGED_CONNECTIONS;

    static {
        SERVER_START_TIME = new Vector<>();
        MANAGED_CONNECTIONS = new ConcurrentHashMap();
        connectNumber = new AtomicInteger(0);
    }

    private NettyServer() {
    }

    /**
     * @param port
     */
    public static void start(int port) {
        MANAGED_CONNECTIONS.clear();
        eventLoopGroupBoss = new NioEventLoopGroup();
        eventLoopGroupWorker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupWorker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(2048));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，开始接收进来的连接
            channelFuture = serverBootstrap.bind(port).sync();

            activity = true;
            SERVER_START_TIME.add(String.format("[%s]@<%s>", DateUtil.formatString(new Date(), DateUtil.SIMPLE_DATE_FORMAT1), null == restartFrom ? "System" : restartFrom));
            restartFrom = null;
            LOG.info(String.format("NIO Server started at %d!", port));

            // 等待服务器  socket 关闭
            channelFuture.channel().closeFuture().sync().addListener(future -> {
                if (future.isSuccess()) {
                    start(-1 == restartPort ? port : restartPort);
                }
            });
        } catch (Exception e) {
            eventLoopGroupWorker.shutdownGracefully();
            eventLoopGroupBoss.shutdownGracefully();
            LOG.error(e);
        }
    }

    /**
     * @param port
     */
    public static void restart(int port, String from) {
        activity = false;
        restartPort = port;
        restartFrom = from;
        eventLoopGroupWorker.shutdownGracefully().addListener(futureWork -> {
            if (futureWork.isSuccess()) {
                eventLoopGroupBoss.shutdownGracefully();
            }
        });

    }

    /**
     * @return
     */
    public static boolean isActivity() {
        return activity;
    }

    /**
     * @return
     */
    public static List<String> getServerStartTime() {
        return SERVER_START_TIME;
    }

    /**
     * @return
     */
    public static AtomicInteger getConnectNumber() {
        return connectNumber;
    }

    public static Optional<ManagedConnection> findManagedConnection(String id) {
        if (MANAGED_CONNECTIONS.containsKey(id)) {
            return Optional.ofNullable(MANAGED_CONNECTIONS.get(id));
        } else {
            return Optional.ofNullable(null);
        }
    }

    /**
     * @param id
     * @param managedConnection
     */
    public static void NewConnection(String id, ManagedConnection managedConnection) {
        MANAGED_CONNECTIONS.put(id, managedConnection);
    }

    /**
     * @param id
     */
    public static void removeConnection(String id) {
        MANAGED_CONNECTIONS.remove(id);
    }

    /**
     * @return
     */
    public static ConcurrentHashMap<String, ManagedConnection> getManagedConnections() {
        return MANAGED_CONNECTIONS;
    }
}
