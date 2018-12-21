package com.syj.tcpentrypoint.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.transport.ServerEndpointConfig;
import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.ExceptionUtils;
import com.syj.tcpentrypoint.util.NamedThreadFactory;
import com.syj.tcpentrypoint.util.NetUtils;
import com.syj.tcpentrypoint.util.ThreadPoolUtils;
import io.netty.channel.Channel;

/**
 * 
*  @des    :Server hendler ,manager the clients connections and dispatch the task to business pool
 * @author:shenyanjun1
 * @date   :2018-12-14 17:12
 */
public class BaseServerHandler implements ServerEndpointHandler {

	/**
	 * slf4j Logger for this class
	 */
	private final static Logger logger = LoggerFactory.getLogger(BaseServerHandler.class);


	//business pool
	private final ExecutorService bizThreadPool; 

	/**
	 * 长连接列表
	 */
	private static final ConcurrentHashMap<String, Channel> channelsMap = new ConcurrentHashMap<String, Channel>();

	/**
	 * Server Transport Config
	 */
	private final ServerEndpointConfig serverTransportConfig;

	/**
	 * Instantiates a new Base server handler.
	 *
	 * @param transportConfig the transport config
	 */
	public BaseServerHandler(ServerEndpointConfig serverTransportConfig) {
		this.serverTransportConfig = serverTransportConfig;
		this.bizThreadPool = initPool();
	}

	public void shutdown() {
		if (!bizThreadPool.isShutdown()) {
			logger.debug("ServerHandler's business thread pool shutdown..");
			bizThreadPool.shutdown();
		}
	}

	public void handlerRequest(Channel channel, Object requestMsg) {
		if (logger.isTraceEnabled()) {
			logger.trace("handler the Request in ServerChannelHandler..");
		}
		RequestMessage msg = (RequestMessage) requestMsg;
		try {
			submitTask(new RETask(msg,channel));
		} catch (Exception e) {
			RpcException rpcException = ExceptionUtils.handlerException(msg.getMsgHeader(), e);
			throw rpcException;
		}
	}

	/**
	 * Submit task.
	 *
	 * @param task the task
	 */
	protected void submitTask(RETask task) {
		bizThreadPool.submit(task);
	}

	
	/**
	 * Add channel.
	 *
	 * @param channel the channel
	 */
	public static void addChannel(Channel channel) {
		String key = getKey(channel);
		channelsMap.put(key, channel);
	}

	/**
	 * Remove channel.
	 *
	 * @param channel the channel
	 * @return the channel
	 */
	public static Channel removeChannel(Channel channel) {
		String key = getKey(channel);
		return channelsMap.remove(key);
	}

	/**
	 * Remove channel by key.
	 *
	 * @param key the key
	 * @return the channel
	 */
	public static Channel removeChannelByKey(String key) {
		return channelsMap.remove(key);
	}

	/**
	 * Get key.
	 *
	 * @param channel the channel
	 * @return the string
	 */
	public static String getKey(Channel channel) {
		InetSocketAddress local = (InetSocketAddress) channel.localAddress();
		InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
		StringBuilder sb = new StringBuilder();
		sb.append(NetUtils.toIpString(address));
		sb.append(":");
		sb.append(address.getPort());
		sb.append(" --> ");
		sb.append(NetUtils.toIpString(local));
		sb.append(":");
		sb.append(local.getPort());

		String key = sb.toString();
		return key;
	}

	/**
	 * Get all channel.
	 *
	 * @return the list
	 */
	public static List<Channel> getAllChannel() {
		Collection<Channel> channels = channelsMap.values();
		List<Channel> channelList = new ArrayList<Channel>(channels);
		return channelList;
	}

	/**
	 * Get channel by key.
	 *
	 * @param key the key
	 * @return the channel
	 */
	public static Channel getChannelByKey(String key) {
		return channelsMap.get(key);
	}

	/**
	 * 丢到业务线程池执行
	 *
	 * @return
	 */
	public ExecutorService getBizThreadPool() {
		return bizThreadPool;
	}

	/**
	 * 得到配置
	 *
	 * @return 配置
	 */
	public ServerEndpointConfig getServerTransportConfig() {
		return serverTransportConfig;
	}

	private   ThreadPoolExecutor initPool( ) {
		int port = serverTransportConfig.getPort();
		// 计算线程池大小
		int minPoolSize = 0; 
		int aliveTime = 0;
		int maxPoolSize = serverTransportConfig.getServerBusinessPoolSize();
		String poolType = serverTransportConfig.getServerBusinessPoolType();
		if (Constants.THREADPOOL_TYPE_FIXED.equals(poolType)) {
			minPoolSize = maxPoolSize;
			aliveTime = 0;
		} else if (Constants.THREADPOOL_TYPE_CACHED.equals(poolType)) {
			minPoolSize = 20;
			maxPoolSize = Math.max(minPoolSize, maxPoolSize);
			aliveTime = 60000;
		} else {
			logger.error("server.threadpool type{} error.", poolType);
		}

		// 初始化队列
		String queueType = serverTransportConfig.getPoolQueueType();
		int queueSize = serverTransportConfig.getPoolQueueSize();
		boolean isPriority = Constants.QUEUE_TYPE_PRIORITY.equals(queueType);
		BlockingQueue<Runnable> configQueue = ThreadPoolUtils.buildQueue(queueSize, isPriority);

		NamedThreadFactory threadFactory = new NamedThreadFactory("RULESENGINE-BZ-" + port, true);
		RejectedExecutionHandler handler = new RejectedExecutionHandler() {
			private int i = 1;
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				if (i++ % 7 == 0) {
					i = 1;
					logger.warn(
							"Task:{} has been reject for ThreadPool exhausted!"
									+ " pool:{}, active:{}, queue:{}, taskcnt: {}",
							new Object[] { r, executor.getPoolSize(), executor.getActiveCount(),
									executor.getQueue().size(), executor.getTaskCount() });
				}
				throw new RejectedExecutionException("Biz thread pool of provider has bean exhausted");
			}
		};
		logger.debug("Build " + poolType + " business pool for port " + port + " [min: " + minPoolSize + " max:"
				+ maxPoolSize + " queueType:" + queueType + " queueSize:" + queueSize + " aliveTime:" + aliveTime
				+ "]");
		return new ThreadPoolExecutor(minPoolSize, maxPoolSize, aliveTime, TimeUnit.MILLISECONDS, configQueue,
				threadFactory, handler);
	}
}