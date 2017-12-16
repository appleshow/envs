package com.aps.env.service;


import com.aps.env.entity.Message;

/**
 * 处理消息
 *
 * @ClassName: DealMessage
 * @Description:TODO
 * @author: AppleShow
 * @date: 2016年10月31日 下午10:34:56
 * @since 1.0.0
 */
public interface DealMessage {
    void saveMessage(Message message);
}
