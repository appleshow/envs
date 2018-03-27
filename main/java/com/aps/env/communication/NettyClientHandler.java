package com.aps.env.communication;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <dl>
 * <dt>com.aps.env.communication.NettyServerHandler</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    DXC Technology </dd>
 * <dd>CreateDate: 2018/3/20</dd>
 * </dl>
 *
 * @author appleshow
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger(NettyClientHandler.class);

    /**
     * @param channelHandlerContext
     * @throws Exception
     * @see ChannelInboundHandlerAdapter#channelRegistered(ChannelHandlerContext)
     */
    @Override
    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
    }

    /**
     * @param channelHandlerContext
     * @throws Exception
     * @see ChannelInboundHandlerAdapter#channelUnregistered(ChannelHandlerContext)
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
    }

    /**
     * @param channelHandlerContext
     * @param message
     * @see ChannelInboundHandlerAdapter#channelRead(ChannelHandlerContext, Object)
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object message) {
    }

    /**
     * @param channelHandlerContext
     * @param throwable
     * @see ChannelInboundHandlerAdapter#exceptionCaught(ChannelHandlerContext, Throwable)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        // 当出现异常就关闭连接
        LOG.error(throwable);
        channelHandlerContext.close();
    }
}
