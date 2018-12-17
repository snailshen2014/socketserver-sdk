package com.syj.tcpentrypoint.transport;

import com.syj.tcpentrypoint.config.ClientConfig;
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
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	private ClientChannelHandler clientChannelHandler;

	private ClientConfig transportConfig;

	public ClientChannelInitializer(ClientConfig transportConfig) {
		this.transportConfig = transportConfig;
		this.clientChannelHandler = new ClientChannelHandler(transportConfig);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new REEncoder());
		pipeline.addLast(new REDecoder(transportConfig.getPayload()));
		pipeline.addLast(Constants.CLIENT_CHANNELHANDLE_NAME, clientChannelHandler);
	}

	public ClientChannelHandler getClientChannelHandler() {
		return clientChannelHandler;
	}
}