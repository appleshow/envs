package com.aps.env.communication;

import com.aps.env.entity.Message;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName: Cache
 * @Description:TODO
 * @author: AppleShow
 * @date: 2016年11月1日 下午8:12:15
 * @since 1.0.0
 */
public class Cache {
    private static final BlockingQueue<Message> cache;

    static {
        cache = new LinkedBlockingQueue<>();
    }

    private Cache() {

    }

    /**
     * @return
     */
    public static boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * @return
     */
    public static int size() {
        return cache.size();
    }

    /**
     * @param message
     * @return
     */
    public static boolean offer(Message message) {
        return cache.offer(message);
    }

    /**
     * @param cacheData
     * @return
     */
    public static int drainTo(Collection<Message> cacheData) {
        return cache.drainTo(cacheData);

    }
}
