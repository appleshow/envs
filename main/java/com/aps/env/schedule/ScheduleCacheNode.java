package com.aps.env.schedule;

import com.aps.env.service.InitDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
public class ScheduleCacheNode {
    @Resource
    private InitDataService initDataService;
    private static final Logger LOG = LogManager.getLogger(ScheduleCacheNode.class);

    /**
     * 从缓存中取出数据并处理
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cacheNode() {
        initDataService.initHbNode();
        LOG.info("Cached Nodes Successfully!");
    }
}
