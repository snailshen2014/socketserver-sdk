package com.syj.tcpentrypoint.client;

import com.syj.tcpentrypoint.transport.ClientEndpointTransport;

/**
 * 
*  @des    : 代表一个连接，封装了Provider和Transport
*  由于长连接复用，transport里的provider不一定是真正的Provider
 * @author:shenyanjun1
 * @date   :2018-12-20 16:51
 */
public class Connection {

    private final Endpoint endPoint;

    private final ClientEndpointTransport transport;

    public Connection(Endpoint endpoint, ClientEndpointTransport transport) {
        this.endPoint = endpoint;
        this.transport = transport;
    }

    public Endpoint getEndPoint() {
        return endPoint;
    }

    public ClientEndpointTransport getTransport() {
        return transport;
    }
}