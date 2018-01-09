package com.aps.env.schedule;

import com.aps.env.service.InitDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <dl>
 * <dt>com.aps.env.schedule.ScheduleDelLatestData</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    AppleShow Technology </dd>
 * <dd>CreateDate: 2018/1/9</dd>
 * </dl>
 *
 * @author appleshow
 */
@Component
public class ScheduleDelLatestData {
    @Resource
    private InitDataService initDataService;
    private static final Logger LOG = LogManager.getLogger(ScheduleDelLatestData.class);

    /**
     * 删除最近数据
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void delHbDataLatest() {
        initDataService.delHbDataLatest();
        LOG.info("Delete Latest Data Successfully!");
    }
}
