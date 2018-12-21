package com.syj.tcpentrypoint.transport;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.error.InitErrorException;
import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.NetUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * @des :客户端连接的建立和销毁等操作
 * @author:shenyanjun1
 * @date :2018-12-17 14:22
 */
public class ClientEndpointFactory {

	/**
	 * Slf4j Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ClientEndpointFactory.class);

	/**
	 * 连接池，一个服务端ip和端口同一协议只建立一个长连接，不管多少接口，共用长连接
	 */
	private final static Map<String, ClientEndpointTransport> connectionPool = new ConcurrentHashMap<String, ClientEndpointTransport>();
	private final static Map<ClientEndpointTransport, AtomicInteger> refCountPool = new ConcurrentHashMap<ClientEndpointTransport, AtomicInteger>();// weak
	private final static ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<String, Object>();
    
	public static ClientEndpointTransport getClientTransport(ClientEndpointConfig config) {
		// 关键字是ip+端口
		String key = NetUtils.getClientTransportKey("RE", config.getProvider().getIp(), config.getProvider().getPort());
		ClientEndpointTransport conn = connectionPool.get(key);
		AtomicInteger count = null;
		if (conn == null) {
			Object lock = lockMap.get(key);
			if (lock == null) {
				lock = new Object();
				Object temp = lockMap.putIfAbsent(key, lock);
				if (temp != null) {
					lock = temp;
				}
			}
			synchronized (lock) {
				conn = connectionPool.get(key);
				if (conn == null) {
					logger.debug("Try connect to provider:{}", config.getProvider());
					conn = initTransport(config);
					connectionPool.put(key, conn);
					count = refCountPool.get(conn);
					if (count == null) {
						count = new AtomicInteger(0);
						refCountPool.put(conn, count);
					}
				}
			}
			lockMap.remove(key);

		}
		count = refCountPool.get(conn);
		int currentCount = count.incrementAndGet();
		logger.debug("Client transport {} of {} , current ref count is: {}", new Object[] { conn,
				NetUtils.channelToString(conn.getLocalAddress(), conn.getRemoteAddress()), currentCount });
		return conn;
	}

	public static Map<String, ClientEndpointTransport> getConnectionPool() {
		return connectionPool;
	}

	/**
	 *
	 * @param clientTransport ClientTransport
	 * @param timeout         等待结果超时时间
	 */
	public static void releaseTransport(ClientEndpointTransport clientTransport, int timeout) {
		if (clientTransport == null) {
			return;
		}
		AtomicInteger integer = refCountPool.get(clientTransport);
		if (integer == null) {
			return;
		} else {
			int currentCount = refCountPool.get(clientTransport).decrementAndGet();
			InetSocketAddress local = clientTransport.getLocalAddress();
			InetSocketAddress remote = clientTransport.getRemoteAddress();
			logger.debug("Client transport {} of {} , current ref count is: {}",
					new Object[] { clientTransport, NetUtils.channelToString(local, remote), currentCount });
			if (currentCount <= 0) { // 此长连接无任何引用
				String ip = NetUtils.toIpString(remote);
				int port = remote.getPort();
				String key = NetUtils.getClientTransportKey( "RE", ip, port);
				logger.info("Shutting down client transport {} now..", NetUtils.channelToString(local, remote));
				connectionPool.remove(key);
				refCountPool.remove(clientTransport);
				if (timeout > 0) {
					int count = clientTransport.currentRequests();
					if (count > 0) { // 有正在调用的请求
						long start = System.currentTimeMillis();
						logger.info("There are {} outstanding call in transport, will shutdown util return", count);
						while (clientTransport.currentRequests() > 0
								&& System.currentTimeMillis() - start < timeout) { // 等待返回结果
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
							}
						}
					} // 关闭前检查已有请求？
				}
				clientTransport.shutdown();
			}
		}

	}

	private static ClientEndpointTransport initTransport(ClientEndpointConfig config) {
		ClientEndpointTransport clientTransport = instanceTransport(config);
		return clientTransport;
	}

	private static ClientEndpointTransport instanceTransport(ClientEndpointConfig config) {
		ClientEndpointTransport clientTransport = null;
		try {
			Channel channel = BuildChannel(config);
			clientTransport = new REClientEndpoint(channel).setClientTransportConfig(config);
			bindTransport(clientTransport, channel);
		} catch (InitErrorException e) {
			logger.debug(e.getMessage(), e);
			throw e;
		}
		return clientTransport;
	}

	private static ClientEndpointTransport bindTransport(ClientEndpointTransport clientTransport, Channel channel) {
		ClientEndpointChannelHandler clientChannelHandler = (ClientEndpointChannelHandler) channel.pipeline()
				.get(Constants.CLIENT_CHANNELHANDLE_NAME);
		clientChannelHandler.setClientTransport((REClientEndpoint) clientTransport);
		return clientTransport;

	}

	public static ClientEndpointTransport reconn(REClientEndpoint clientTransport) {
		Channel channel = ClientEndpointFactory.BuildChannel(clientTransport.getConfig());
		ClientEndpointFactory.bindTransport(clientTransport, channel);
		clientTransport.setChannel(channel);
		return clientTransport;
	}

	public static Channel BuildChannel(ClientEndpointConfig transportConfig) {
		EventLoopGroup eventLoopGroup = transportConfig.getEventLoopGroup(transportConfig);
		Channel channel = null;
		String host = transportConfig.getProvider().getIp();
		int port = transportConfig.getProvider().getPort();
		int connectTimeout = transportConfig.getConnectTimeout();
		Class clazz = NioSocketChannel.class;
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventLoopGroup).channel(clazz);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.option(ChannelOption.ALLOCATOR, PooledBufHolder.getInstance());
			bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
			bootstrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
			bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
			ClientEndpointChannelInitializer initializer = new ClientEndpointChannelInitializer(transportConfig);
			bootstrap.handler(initializer);
			// Bind and start to accept incoming connections.

			ChannelFuture channelFuture = bootstrap.connect(host, port);
			channelFuture.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
			if (channelFuture.isSuccess()) {
				channel = channelFuture.channel();
				if (NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress())
						.equals(NetUtils.toAddressString((InetSocketAddress) channel.localAddress()))) {
					// 服务端不存活时，连接左右两侧地址一样的情况
					channel.close(); // 关掉重连
					throw new InitErrorException("Failed to connect " + host + ":" + port
							+ ". Cause by: Remote and local address are the same");
				}
			} else {
				Throwable cause = channelFuture.cause();
				throw new InitErrorException("Failed to connect " + host + ":" + port
						+ (cause != null ? ". Cause by: " + cause.getMessage() : "."));
			}
		} catch (InitErrorException e) {
			throw e;
		} catch (Exception e) {
			String errorStr = "Failed to build channel for host:" + host + " port:" + port + ". Cause by: "
					+ e.getMessage();
			InitErrorException initException = new InitErrorException(errorStr, e);
			throw initException;
		}
		return channel;
	}

	public static REClientEndpoint getTransportByKey(String key) {
		return (REClientEndpoint) connectionPool.get(key);
	}

	/**
	 * 关闭全部客户端连接
	 */
	public static void closeAll() {
		logger.info("Shutdown all  client transport now...");
		try {
			for (Map.Entry<String, ClientEndpointTransport> entrySet : connectionPool.entrySet()) {
				ClientEndpointTransport clientTransport = entrySet.getValue();
				if (clientTransport.isOpen()) {
					clientTransport.shutdown();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			ClientEndpointConfig.closeEventGroup();
		}
	}

	/**
	 * 检查Future列表，删除超时请求
	 */
	public static void checkFuture() {
		for (Map.Entry<String, ClientEndpointTransport> entrySet : connectionPool.entrySet()) {
			try {
				ClientEndpointTransport clientTransport = entrySet.getValue();
				if (clientTransport instanceof AbstractTCPClientEndpoint) {
					AbstractTCPClientEndpoint aClientTransport = (AbstractTCPClientEndpoint) clientTransport;
//					aClientTransport.checkFutureMap();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}