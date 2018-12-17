package com.syj.tcpentrypoint.client;

import com.syj.tcpentrypoint.transport.ClientTransport;

/**
 * Title: 代表一个连接，封装了Provider和Transport<br>
 * <p/>
 * Description: 由于长连接复用，transport里的provider不一定是真正的Provider <br>
 * <p/>
 */
public class Connection {

    private final Provider provider;

    private final ClientTransport transport;

    public Connection(Provider provider, ClientTransport transport) {
        this.provider = provider;
        this.transport = transport;
    }

    public Provider getProvider() {
        return provider;
    }

    public ClientTransport getTransport() {
        return transport;
    }
}