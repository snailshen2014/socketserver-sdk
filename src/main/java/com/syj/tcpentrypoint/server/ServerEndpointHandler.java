package com.syj.tcpentrypoint.server;

import io.netty.channel.Channel;

/**
 * 
*  @des    :Server handler
 * @author:shenyanjun1
 * @date   :2018-12-14 17:14
 */
public interface ServerEndpointHandler {

    /**
     * 处理请求（可以实时或者丢到线程池）
     *
     * @param channel
     *         连接（结果可以写入channel）
     * @param requestMsg
     *         请求
     */
    void handlerRequest(Channel channel, Object requestMsg);

    /**
     * 关闭服务
     */
    void shutdown();

}