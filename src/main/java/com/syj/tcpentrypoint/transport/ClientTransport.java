package com.syj.tcpentrypoint.transport;




import java.net.InetSocketAddress;

import com.syj.tcpentrypoint.config.ClientConfig;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;

/**
 * Title: 客户端服务端连接<br>
 * <p/>
 * Description: 处理建立/断开连接，收到数据等事件<br>
 * <p/>
 */
public interface ClientTransport {

    /**
     * 重连
     */
    void reconnect();

    /**
     * 关闭
     */
    void shutdown();

    /**
     * 得到配置
     *
     * @return ClientTransportConfig
     */
    ClientConfig getConfig();

    /**
     * 异步调用
     *
     * @param msg
     *         the msg 消息
     * @param timeout
     *         the timeout 超时时间
     * @return 异步Future
     */
//    MsgFuture sendAsyn(BaseMessage msg, int timeout);

    /**
     * 同步调用
     *
     * @param msg
     *         the msg 消息
     * @param timeout
     *         the timeout 超时时间
     * @return ResponseMessage
     */
    ResponseMessage send(BaseMessage msg, int timeout);

    /**
     * 是否开启
     *
     * @return the boolean
     */
    boolean isOpen();

    /**
     * 得到连接的远端地址
     *
     * @return the remote address
     */
    InetSocketAddress getRemoteAddress();

    /**
     * 得到连接的本地地址（如果是短连接，可能不准）
     *
     * @return the local address
     */
    InetSocketAddress getLocalAddress();

    /**
     * 当前请求数
     *
     * @return 当前请求数
     */
    int currentRequests();
}