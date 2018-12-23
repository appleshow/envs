package com.aps.env.communication;

import com.aps.env.comm.CommUtil;
import com.aps.env.comm.DateUtil;
import com.aps.env.comm.StringUtil;
import com.aps.env.entity.ManagedConnection;
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

        final String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        final String id = channelHandlerContext.channel().id().asShortText();
        NettyServer.getConnectNumber().incrementAndGet();
        NettyServer.NewConnection(id, new ManagedConnection(id, remoteAddress));
        LOG.info(String.format("Session has been opened: %s", remoteAddress));
    }

    /**
     * @param channelHandlerContext
     * @throws Exception
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelUnregistered(ChannelHandlerContext)
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelUnregistered(channelHandlerContext);

        final String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        final String id = channelHandlerContext.channel().id().asShortText();
        NettyServer.getConnectNumber().decrementAndGet();
        NettyServer.removeConnection(id);
        LOG.info(String.format("Session has been closed: %s", null == remoteAddress ? id : remoteAddress));
    }

    /**
     * @param channelHandlerContext
     * @param msg
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(ChannelHandlerContext, Object)
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) {
        final String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        final String id = channelHandlerContext.channel().id().asShortText();

        final Message message = new Message();
        message.setId(id);
        message.setMessageBody(String.valueOf(msg));
        message.setReceiveDate(DateUtil.formatString(new Date(), DateUtil.SIMPLE_DATE_FORMAT1));
        message.setFromHost(remoteAddress);
        message.increaseTryTimes();

        NettyServer.findManagedConnection(id).ifPresent(connection -> connection.setActiveAt(message.getReceiveDate()));

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

        final String id = channelHandlerContext.channel().id().asShortText();
        if (!StringUtil.isNullOrEmpty(id)) {
            NettyServer.removeConnection(id);
        }

        channelHandlerContext.close();
    }
}
