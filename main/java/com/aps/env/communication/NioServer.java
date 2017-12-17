package com.aps.env.communication;

import com.aps.env.comm.CommUtil;
import com.aps.env.comm.DateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.api.IoSession;
import org.apache.mina.transport.nio.NioTcpServer;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: NioServer
 * @Description:TODO
 * @author: AppleShow
 * @date: 2016年10月28日 下午10:14:44
 * @since 1.0.0
 */
public class NioServer {
    private static String serverStartTime;
    private static NioTcpServer NIOTCPSERVER = new NioTcpServer();
    private static final Logger LOG = LogManager.getLogger(NioServer.class);

    private NioServer() {

    }

    /**
     * @return boolean
     * @Title: start
     * @Description: TODO
     * @throws:
     * @since 1.0.0
     */
    public static boolean start(int port) {
        if (!NIOTCPSERVER.isActive()) {
            final SocketAddress socketAddress = new InetSocketAddress(port);
            NIOTCPSERVER.setFilters(new NioServerFilter());
            NIOTCPSERVER.setIoHandler(new NioServerIoHandler());
            NIOTCPSERVER.bind(socketAddress);

            serverStartTime = DateUtil.formatString(new Date(), DateUtil.SIMPLE_DATE_FORMAT1);
            LOG.info("Server started: " + port);
            return true;
        } else {
            LOG.info("Server has been started!");
            return false;
        }
    }

    /**
     * @return boolean
     * @Title: stop
     * @Description: TODO
     * @throws:
     * @since 1.0.0
     */
    public static boolean stop() {
        if (NIOTCPSERVER != null) {
            try {
                NIOTCPSERVER.unbind();
            } catch (Exception e) {
                LOG.info(e);
            }
            NIOTCPSERVER = null;
            NIOTCPSERVER = new NioTcpServer();
            LOG.info("Server stoped!");

            return true;
        } else {
            LOG.info("Server has been stoped!");
            return false;
        }
    }

    public static Map<Long, IoSession> getManagedSessions() {
        return NIOTCPSERVER.getManagedSessions();
    }

    /**
     * @return
     */
    public static NioTcpServer getNioTcpServer() {
        return NIOTCPSERVER;
    }

    /**
     * @return String
     * @Title: getServerStartTime
     * @Description: TODO
     * @throws:
     * @since 1.0.0
     */
    public static String getServerStartTime() {
        if (!NIOTCPSERVER.isDisposed()) {
            return serverStartTime;
        } else {
            return "----";
        }
    }
}
