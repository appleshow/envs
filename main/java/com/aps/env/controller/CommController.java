package com.aps.env.controller;

import com.aps.env.comm.StringUtil;
import com.aps.env.communication.Cache;
import com.aps.env.communication.NettyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * <dl>
 * <dt>com.aps.env.controller.CommController</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    DXC Technology </dd>
 * <dd>CreateDate: 2018/3/19</dd>
 * </dl>
 *
 * @author appleshow
 */
@RestController
@RequestMapping(value = "/")
public class CommController extends ExceptionController {
    private static final Logger LOG = LogManager.getLogger(CommController.class);

    @Autowired
    private Environment environment;

    /**
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "getOnlineClient", method = RequestMethod.GET)
    public List<String> getOnlineClient(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        LOG.info(String.format("Call getOnlineClient from: %s", httpServletRequest.getRemoteAddr()));
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        final Map<String, String> onlineClient = NettyServer.getManagedConnections();
        final Map<String, String> mapClient = new HashMap<>();
        final List<String> returnClient = new ArrayList<>();

        onlineClient.forEach((k, v) -> {
            final String[] client = v.split(",");

            mapClient.put(k, v);
            NettyServer.getManagedNode().forEach((kn, vn) -> {
                if (client[1].equals(vn)) {
                    mapClient.put(k, String.format("%s,[%s]", mapClient.get(k), kn));
                }
            });
        });

        mapClient.forEach(
                (k, v) -> returnClient.add(v));

        return returnClient;
    }

    /**
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "getConnectionNumber", method = RequestMethod.GET)
    public List<String> getConnectionNumber(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final List<String> connectionNumber = new ArrayList<>();
        LOG.info(String.format("Call getConnectionNumber from: %s", httpServletRequest.getRemoteAddr()));

        connectionNumber.add(String.format("Connection Number: %d", NettyServer.getConnectNumber().get()));
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        return connectionNumber;
    }

    /**
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "getCacheSize", method = RequestMethod.GET)
    public List<String> getCacheSize(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final List<String> cacheSize = new ArrayList<>();
        LOG.info(String.format("Call getCacheSize from: %s", httpServletRequest.getRemoteAddr()));

        cacheSize.add(String.format("%s%d", "Cached:", Cache.size()));
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        return cacheSize;
    }

    /**
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "restartNIO", method = RequestMethod.GET)
    public List<String> restartNIO(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final List<String> restart = new ArrayList<>();
        int checkTimes = 0;

        LOG.info(String.format("Call restartNIO from: %s", httpServletRequest.getRemoteAddr()));
        NettyServer.restart(Integer.parseInt(environment.getProperty("nioTcpPort")), httpServletRequest.getRemoteAddr());

        while (!NettyServer.isActivity()) {
            checkTimes++;
            if (checkTimes >= 61) {
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
        if (NettyServer.isActivity()) {
            restart.add("NIO Server has bean restarted!");
        } else {
            restart.add("Try to start NIO Server failed!");
        }

        LOG.info(restart.get(0));
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        return restart;
    }

    /**
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "getServerStartTime", method = RequestMethod.GET)
    public List<String> getServerStartTime(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        LOG.info(String.format("Call getServerStartTime from: %s", httpServletRequest.getRemoteAddr()));
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");


        return NettyServer.getServerStartTime();
    }

}
