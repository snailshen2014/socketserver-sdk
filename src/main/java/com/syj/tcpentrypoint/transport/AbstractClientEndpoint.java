package com.syj.tcpentrypoint.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.config.ClientEndpointConfig;

/**
 * Title: 客户端连接抽象类<br>
 * <p/>
 * Description: 只是简单封装了地址。配置等信息<br>
 * <p/>
 */
abstract class AbstractClientEndpoint implements ClientEndpointTransport {

    /**
     * slf4j logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractClientEndpoint.class);

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
    protected ClientEndpointConfig clientEndpointConfig;

    /**
     * 构造函数
     *
     * @param clientTransportConfig
     *         客户端配置
     */
    protected AbstractClientEndpoint(ClientEndpointConfig clientEndpointConfig) {
        this.clientEndpointConfig = clientEndpointConfig;
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
    public ClientEndpointConfig getConfig() {
        return clientEndpointConfig;
    }

    @Override
    public int currentRequests() {
        return currentRequests.get();
    }
}