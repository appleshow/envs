package com.aps.env.communication;

import com.aps.env.comm.CommUtil;
import com.aps.env.comm.DateUtil;
import com.aps.env.entity.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

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
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger(NettyServerHandler.class);

    /**
     * @param channelHandlerContext
     * @throws Exception
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRegistered(ChannelHandlerContext)
     */
    @Override
    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelRegistered(channelHandlerContext);

        String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        NettyServer.getConnectNumber().incrementAndGet();
        NettyServer.inputChannelAddress(remoteAddress, channelHandlerContext.channel().id().asLongText());
        LOG.info(String.format("Session has been opened: %s", CommUtil.formatHost(remoteAddress, null)));
    }

    /**
     * @param channelHandlerContext
     * @throws Exception
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelUnregistered(ChannelHandlerContext)
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelUnregistered(channelHandlerContext);

        String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        NettyServer.getConnectNumber().decrementAndGet();
        NettyServer.removeChannelAddress(remoteAddress, channelHandlerContext.channel().id().asLongText());
        LOG.info(String.format("Session has been closed: %s", CommUtil.formatHost(remoteAddress, null)));
    }

    /**
     * @param channelHandlerContext
     * @param msg
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(ChannelHandlerContext, Object)
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) {
        String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();

        Message message = new Message();
        message.setMessageBody(String.valueOf(msg));
        message.setReceiveDate(DateUtil.formatString(new Date(), DateUtil.SIMPLE_DATE_FORMAT1));
        message.setFromHost(CommUtil.formatHost(remoteAddress, null));
        message.increaseTryTimes();
        message.setChannelId(channelHandlerContext.channel().id().asLongText());

        Cache.offer(message);
        LOG.debug("Received [" + message.getFromHost() + "] -> " + msg);
    }

    /**
     * @param channelHandlerContext
     * @param throwable
     * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(ChannelHandlerContext, Throwable)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        // 当出现异常就关闭连接
        LOG.error(throwable);
        channelHandlerContext.close();
    }
}
