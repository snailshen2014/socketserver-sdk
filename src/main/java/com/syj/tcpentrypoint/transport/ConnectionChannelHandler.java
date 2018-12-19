package com.syj.tcpentrypoint.transport;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.util.NetUtils;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
*  @des    :The connections manger
 * @author:shenyanjun1
 * @date   :2018-12-14 17:15
 */
@ChannelHandler.Sharable
public class ConnectionChannelHandler extends ChannelInboundHandlerAdapter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectionChannelHandler.class);

    private final ServerTransportConfig transportConfig;

    public ConnectionChannelHandler(ServerTransportConfig transportConfig) {
        this.transportConfig = transportConfig;
    }

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        /*
         * 最好其实是实现channelActive方法，
         * 但是AdapterDecoder会重新fireChannelActive，导致重复执行，所以用此事件
         */
        int now = counter.incrementAndGet();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Connected from {}, now connection is {}",
                    NetUtils.channelToString(ctx.channel().remoteAddress(), ctx.channel().localAddress()), now);
        }
        // 刚建立连接直接计数器加一，不管是长连接
        if (now > transportConfig.getMaxConnection()) {
            LOGGER.error("Maximum connection {} have been reached, cannot create channel any more",
                    transportConfig.getMaxConnection());
            ctx.channel().close();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        int now = counter.decrementAndGet();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Disconnected from {}, now connection is {}",
                    NetUtils.channelToString(ctx.channel().remoteAddress(), ctx.channel().localAddress()), now);
        }
    }
}