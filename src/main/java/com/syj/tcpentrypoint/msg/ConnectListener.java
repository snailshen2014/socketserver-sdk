package com.syj.tcpentrypoint.msg;



import io.netty.channel.ChannelHandlerContext;

/**
 * Title: server连接事件<br>
 * <p/>
 * Description: Server端当有客户端建立连接和销毁连接的时候，注意只做一些快速的动作，防止阻塞IO线程<br>
 * <p/>
 */
public interface ConnectListener {

    /**
     * 客户端建立连接的时候，服务端触发的事件
     *
     * @param ctx the ctx
     */
    public void connected(ChannelHandlerContext ctx);

    /**
     * 客户端断开连接的时候，服务端触发的事件
     *
     * @param ctx the ctx
     */
    public void disconnected(ChannelHandlerContext ctx);
}