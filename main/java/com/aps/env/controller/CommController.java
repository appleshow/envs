package com.aps.env.controller;

import com.aps.env.comm.CommUtil;
import com.aps.env.communication.Cache;
import com.aps.env.communication.NettyServer;
import com.aps.env.entity.HbTypeItemNode;
import com.aps.env.entity.ManagedConnection;
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

    @RequestMapping(value = "help", method = RequestMethod.GET)
    public List<String> getHelp(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return new ArrayList<String>() {{
            add("getOnlineClient");
            add("getConnectionNumber");
            add("getCacheSize");
            add("getServerStartTime");
            add("getCachedHbNodeMN");
            add("getCachedHbNodeName");
            add("getCachedHbNodeItemS");
            add("getCachedHbNodeItemO");
        }};
    }

    /**
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "getOnlineClient", method = RequestMethod.GET)
    public Map<String, ManagedConnection> getOnlineClient(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        LOG.info(String.format("Call getOnlineClient from: %s", httpServletRequest.getRemoteAddr()));
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        return NettyServer.getManagedConnections();
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

    /**
     * @return
     */
    @RequestMapping(value = "getCachedHbNodeMN", method = RequestMethod.GET)
    public Map<String, Integer> getCachedHbNodeMN() {
        return CommUtil.getHbNodeCache();
    }

    /**
     * @return
     */
    @RequestMapping(value = "getCachedHbNodeName", method = RequestMethod.GET)
    public Map<Integer, String> getCachedHbNodeName() {
        return CommUtil.getNodeNameCache();
    }

    /**
     * @return
     */
    @RequestMapping(value = "getCachedHbNodeItemS", method = RequestMethod.GET)
    public Map<Integer, String> getCachedHbNodeItemS() {
        return CommUtil.getNodeTypeItemCacheS();
    }

    /**
     * @return
     */
    @RequestMapping(value = "getCachedHbNodeItemO", method = RequestMethod.GET)
    public Map<Integer, List<HbTypeItemNode>> getCachedHbNodeItemO() {
        return CommUtil.getNodeTypeItemCacheO();
    }
}
