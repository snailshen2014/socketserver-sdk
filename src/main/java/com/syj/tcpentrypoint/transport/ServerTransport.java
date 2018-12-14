package com.syj.tcpentrypoint.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;

import com.syj.tcpentrypoint.error.InitErrorException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * 
*  @des    :socket server base the netty
 * @author:shenyanjun1
 * @date   :2018-12-14 17:20
 */
public class ServerTransport {
	private static final Logger logger = LoggerFactory.getLogger(ServerTransport.class);
	private ServerTransportConfig transportConfig;
	private ServerBootstrap serverBootstrap;

	public ServerTransport(ServerTransportConfig config) {
		this.transportConfig = config;
	}

	/**
	 * 
	 */
	public Boolean start() {
		boolean flag = Boolean.FALSE;
		logger.info("Server transport start! ");
		Class clazz = NioServerSocketChannel.class;

		if (transportConfig.isUseEpoll()) {
			clazz = EpollServerSocketChannel.class;
		}
		Boolean reusePort = Boolean.TRUE;
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(transportConfig.getParentEventLoopGroup(), transportConfig.getChildEventLoopGroup()).channel(clazz)
				.childHandler(new ServerChannelInitializer(transportConfig));
		serverBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, transportConfig.getCONNECTTIMEOUT())
				.option(ChannelOption.SO_BACKLOG, transportConfig.getBACKLOG()).option(ChannelOption.SO_REUSEADDR, reusePort) // disable
																														// this
																														// on
																														// windows,
																														// open
																														// it
																														// on
																														// linux
				.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
				.childOption(ChannelOption.SO_KEEPALIVE, transportConfig.isKEEPALIVE())
				// .childOption(ChannelOption.SO_TIMEOUT, config.getTIMEOUT())
				.childOption(ChannelOption.TCP_NODELAY, transportConfig.isTCPNODELAY())
				.childOption(ChannelOption.ALLOCATOR, PooledBufHolder.getInstance())
				.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
				.childOption(ChannelOption.SO_RCVBUF, 8192 * 128).childOption(ChannelOption.SO_SNDBUF, 8192 * 128)
				.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);

		// 绑定到全部网卡 或者 指定网卡
		ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(transportConfig.getHost(), transportConfig.getPort()));
		ChannelFuture channelFuture = future.addListener(new ChannelFutureListener() {
			
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					logger.info("Server have success bind to {}:{}", transportConfig.getHost(), transportConfig.getPort());

				} else {
					logger.error("Server fail bind to {}:{}", transportConfig.getHost(), transportConfig.getPort());
					transportConfig.getParentEventLoopGroup().shutdownGracefully();
					transportConfig.getChildEventLoopGroup().shutdownGracefully();
					throw new InitErrorException("Server start fail !", future.cause());
				}

			}
		});

		try {
			channelFuture.await(5000, TimeUnit.MILLISECONDS);
			if (channelFuture.isSuccess()) {
				flag = Boolean.TRUE;
			}

		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		return flag;
	}

	public void stop() {
		logger.info("Shutdown the RE server transport now...");
		transportConfig.getParentEventLoopGroup().shutdownGracefully();
		transportConfig.getChildEventLoopGroup().shutdownGracefully();

	}
}
