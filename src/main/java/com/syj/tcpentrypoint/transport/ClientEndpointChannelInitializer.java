package com.syj.tcpentrypoint.transport;

import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.re.REDecoder;
import com.syj.tcpentrypoint.re.REEncoder;
import com.syj.tcpentrypoint.util.Constants;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Title: 初始化客户端的ChannelPipeline<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
@ChannelHandler.Sharable
public class ClientEndpointChannelInitializer extends ChannelInitializer<SocketChannel> {

	private ClientEndpointChannelHandler clientChannelHandler;

	private ClientEndpointConfig transportConfig;

	public ClientEndpointChannelInitializer(ClientEndpointConfig transportConfig) {
		this.transportConfig = transportConfig;
		this.clientChannelHandler = new ClientEndpointChannelHandler(transportConfig);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new REEncoder());
		pipeline.addLast(new REDecoder(transportConfig.getPayload()));
		pipeline.addLast(Constants.CLIENT_CHANNELHANDLE_NAME, clientChannelHandler);
	}

	public ClientEndpointChannelHandler getClientChannelHandler() {
		return clientChannelHandler;
	}
}