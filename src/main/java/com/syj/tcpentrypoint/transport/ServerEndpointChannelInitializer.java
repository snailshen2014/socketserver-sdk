package com.syj.tcpentrypoint.transport;

import com.syj.tcpentrypoint.codec.AdapterDecoder;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 
*  @des    :Init the server ChannelPipeline
 * @author:shenyanjun1
 * @date   :2018-12-14 17:18
 */
@ChannelHandler.Sharable
public class ServerEndpointChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ServerEndpointChannelHandler serverChannelHandler;

    private ServerConnectionChannelHandler connectionChannelHandler;

    private ServerEndpointConfig transportConfig;

    public ServerEndpointChannelInitializer(ServerEndpointConfig transportConfig){
        this.transportConfig = transportConfig;
        this.serverChannelHandler = new ServerEndpointChannelHandler(transportConfig);
        this.connectionChannelHandler = new ServerConnectionChannelHandler(transportConfig);
    }
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 只保留一个 根据第一次请求识别协议，构建后面的ChannelHandler
        ch.pipeline().addLast(connectionChannelHandler)
        				  .addLast(new AdapterDecoder(serverChannelHandler,transportConfig.getPayload()));
    }
}