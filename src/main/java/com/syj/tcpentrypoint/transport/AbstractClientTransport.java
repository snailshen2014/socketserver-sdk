package com.syj.tcpentrypoint.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.config.ClientConfig;

/**
 * Title: 客户端连接抽象类<br>
 * <p/>
 * Description: 只是简单封装了地址。配置等信息<br>
 * <p/>
 */
abstract class AbstractClientTransport implements ClientTransport {

    /**
     * slf4j logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractClientTransport.class);

    /**
     * 本地地址
     */
    protected SocketAddress localAddress;

    /**
     * 远程地址
     */
    protected SocketAddress remoteAddress;

    /**
     * 正在发送的调用数量
     */
    protected AtomicInteger currentRequests = new AtomicInteger(0);

    /**
     * 当前的客户端配置
     */
    protected ClientConfig clientTransportConfig;

    /**
     * 构造函数
     *
     * @param clientTransportConfig
     *         客户端配置
     */
    protected AbstractClientTransport(ClientConfig clientTransportConfig) {
        this.clientTransportConfig = clientTransportConfig;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) this.remoteAddress;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) this.localAddress;
    }

    @Override
    public ClientConfig getConfig() {
        return clientTransportConfig;
    }

    @Override
    public int currentRequests() {
        return currentRequests.get();
    }
}