package com.aps.env.processing;


import com.aps.env.entity.Message;

/**
 * 处理消息
 *
 * @ClassName: ProcessMessage
 * @Description:TODO
 * @author: AppleShow
 * @date: 2016年10月31日 下午10:34:56
 * @since 1.0.0
 */
public interface ProcessMessage {
    void saveMessage(Message message);
}
