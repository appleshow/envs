package com.aps.env.communication;

import com.aps.env.comm.CommUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.api.AbstractIoFilter;
import org.apache.mina.api.AbstractIoHandler;
import org.apache.mina.api.IoFuture;
import org.apache.mina.api.IoSession;
import org.apache.mina.transport.nio.NioTcpClient;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;

/**
 * @ClassName: NioClient
 * @Description:TODO
 * @author: AppleShow
 * @date: 2016年11月7日 下午8:29:58
 * @since 1.0.0
 */
public class NioClient {
    private int port;
    private NioTcpClient nioTcpClient;
    private static final Logger LOG = LogManager.getLogger(NioClient.class);

    public NioClient(int port) {
        this.port = port;
        this.nioTcpClient = new NioTcpClient();
    }

    /**
     * @return boolean
     * @Title: tryToConnectServer
     * @Description: TODO
     * @throws:
     * @since 1.0.0
     */
    public boolean tryToConnectServer() {
        nioTcpClient.setFilters(new AbstractIoFilter() {
        });
        nioTcpClient.setIoHandler(new AbstractIoHandler() {
        });

        try {
            IoFuture<IoSession> ioFuture = nioTcpClient.connect(new InetSocketAddress("localhost", port));
            IoSession ioSession = ioFuture.get();
            Thread.sleep(1000);
            ioSession.close(true);
            ioFuture.cancel(true);
            nioTcpClient.disconnect();
            nioTcpClient = null;
        } catch (Exception e) {
            LOG.error(e);
            return false;
        }
        return true;
    }
}
