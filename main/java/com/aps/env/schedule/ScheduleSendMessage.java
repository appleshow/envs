package com.aps.env.schedule;

import com.aps.env.service.SendMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

/**
 *
 */
@Component
public class ScheduleSendMessage {
    @Resource(name = "sendMessage2AliCloud")
    private SendMessage sendMessage;
    private static final Logger LOG = LogManager.getLogger(ScheduleSendMessage.class);

    @Scheduled(cron = "${sendMessageCron}")
    public void sendMessage() throws UnsupportedEncodingException {
        String info = sendMessage.sendMessage();
        LOG.info(info);
    }
}
