package com.aps.env.schedule;

import com.aps.env.communication.Cache;
import com.aps.env.communication.CacheOperation;
import com.aps.env.entity.Message;
import com.aps.env.service.DealMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

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
public class SchedulePullData {
    @Resource(name = "dealMessage212")
    private DealMessage dealMessage;
    private static final Logger LOG = LogManager.getLogger(SchedulePullData.class);

    /**
     * 从缓存中取出数据并处理
     */
    @Scheduled(cron = "30 * * * * ?")
    public void pullData() {
        LOG.info("Loading data from cache...");
        final Collection<Message> cacheData = new ArrayList<>();
        final CacheOperation cacheOperation = new CacheOperation();

        if (!Cache.getCache().isEmpty()) {
            Cache.getCache().drainTo(cacheData);
            cacheData.stream().forEach(message -> {
                try {
                    message.increaseTryTimes();
                    dealMessage.saveMessage(message);
                    cacheOperation.operationSuccess();
                } catch (Exception e) {
                    cacheOperation.operationFailed();
                    if (message.getTryTimes() >= 5) {
                        LOG.error("Deal the message failed 5 times, remove it. Message: " + message.getMessageBodyTailor());
                    } else {
                        Cache.getCache().offer(message);
                    }
                    LOG.error(e);
                }
            });
        }
        LOG.info(String.format("Loaded data from cache. Total: %d; Success: %d; Failed: %d.", cacheOperation.getTotal(), cacheOperation.getSuccess(),
                cacheOperation.getFailed()));
    }
}
