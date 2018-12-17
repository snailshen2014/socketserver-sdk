package com.syj.tcpentrypoint.config;

import java.util.List;

import com.syj.tcpentrypoint.client.Provider;
import com.syj.tcpentrypoint.msg.ConnectListener;
import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.NamedThreadFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * 
 * @des :抽象出配置到一层
 * @author:shenyanjun1
 * @date :2018-12-17 10:30
 * @param <T>
 */
public class ClientConfig {

	/*---------- 参数配置项开始 ------------*/

	/**
	 * 远程调用超时时间(毫秒)
	 */
	private int invokeTimeout = Constants.DEFAULT_CLIENT_INVOKE_TIMEOUT;

	/**
	 * 连接超时时间
	 */
	private int connectTimeout = Constants.DEFAULT_CLIENT_CONNECT_TIMEOUT;

	/**
	 * 关闭超时时间（如果还有请求，会等待请求结束或者超时）
	 */
	private int disconnectTimeout = Constants.DEFAULT_CLIENT_DISCONNECT_TIMEOUT;

	/**
	 * 线程池类型
	 */
	private String clientBusinessPoolType = Constants.THREADPOOL_TYPE_CACHED;

	/**
	 * 业务线程池大小
	 */
	protected int clientBusinessPoolSize = Constants.DEFAULT_CLIENT_BIZ_THREADS;

	/**
	 * io线程池大小
	 */
	protected int iothreads;
	private int childNioEventThreads = 0; // worker线程==IO线程

	/**
	 * Consumer给Provider发心跳的间隔
	 */
	protected int heartbeat = Constants.DEFAULT_HEARTBEAT_TIME;

	/**
	 * Consumer给Provider重连的间隔
	 */
	protected int reconnect = Constants.DEFAULT_RECONNECT_TIME;

	/**
	 * 最大数据包大小
	 */
	protected int payload = Constants.DEFAULT_PAYLOAD;
    /**
     * The Loadbalance. 负载均衡
     */
    protected String loadbalance = Constants.LOADBALANCE_RANDOM;
    
    private List<ConnectListener> connectListeners; // 连接事件监听器
    
	private Provider provider; // 对应的Provider信息
	/* server list ip:port;ip:port; */
	protected String serverList;
	/* client app name */
	protected String appName;

	private static EventLoopGroup eventLoopGroup; // 全局共用

	public String getAppName() {
		return this.appName;
	}

	public void setAppName(String apName) {
		this.appName = apName;
	}

	public String getServerList() {
		return this.serverList;
	}

	public void setServerList(String svrList) {
		this.serverList = svrList;
	}

	/**
	 * Gets iothreads.
	 *
	 * @return the iothreads
	 */
	public int getIothreads() {
		return iothreads;
	}

	/**
	 * Sets iothreads.
	 *
	 * @param iothreads the iothreads
	 */
	public void setIothreads(int iothreads) {
		this.iothreads = iothreads;
	}

	/**
	 * Gets connect timeout.
	 *
	 * @return the connect timeout
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * Sets connect timeout.
	 *
	 * @param connectTimeout the connect timeout
	 */
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * Gets disconnect timeout.
	 *
	 * @return the disconnect timeout
	 */
	public int getDisconnectTimeout() {
		return disconnectTimeout;
	}

	/**
	 * Sets disconnect timeout.
	 *
	 * @param disconnectTimeout the disconnect timeout
	 */
	public void setDisconnectTimeout(int disconnectTimeout) {
		this.disconnectTimeout = disconnectTimeout;
	}

	/**
	 * Gets reconnect.
	 *
	 * @return the reconnect
	 */
	public int getReconnect() {
		return reconnect;
	}

	/**
	 * Sets reconnect.
	 *
	 * @param reconnect the reconnect
	 */
	public void setReconnect(int reconnect) {
		this.reconnect = reconnect;
	}

	/**
	 * Gets heartbeat.
	 *
	 * @return the heartbeat
	 */
	public int getHeartbeat() {
		return heartbeat;
	}

	/**
	 * Sets heartbeat.
	 *
	 * @param heartbeat the heartbeat
	 */
	public void setHeartbeat(int heartbeat) {
		this.heartbeat = heartbeat;
	}

	/**
	 * Gets payload.
	 *
	 * @return the payload
	 */
	public int getPayload() {
		return payload;
	}

	/**
	 * Sets payload.
	 *
	 * @param payload the payload
	 */
	public void setPayload(int payload) {
		this.payload = payload;
	}

	public int getInvokeTimeout() {
		return invokeTimeout;
	}

	public void setInvokeTimeout(int invokeTimeout) {
		this.invokeTimeout = invokeTimeout;
	}

	public String getClientBusinessPoolType() {
		return clientBusinessPoolType;
	}

	public void setClientBusinessPoolType(String clientBusinessPoolType) {
		this.clientBusinessPoolType = clientBusinessPoolType;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public static EventLoopGroup getEventLoopGroup(ClientConfig transportConfig) {
		if (eventLoopGroup == null || eventLoopGroup.isShutdown()) {
			initEventLoop(transportConfig);
		}
		return eventLoopGroup;
	}

	private static synchronized void initEventLoop(ClientConfig transportConfig) {
		if (eventLoopGroup == null || eventLoopGroup.isShutdown()) {
			int childNioEventThreads = transportConfig.getChildNioEventThreads();
			int threads = childNioEventThreads > 0 ? childNioEventThreads : // 用户配置
					Math.max(6, Constants.DEFAULT_IO_THREADS); // 默认cpu+1,至少6个
			NamedThreadFactory threadName = new NamedThreadFactory("JSF-CLI-WORKER", true);
			eventLoopGroup = new NioEventLoopGroup(threads, threadName);

		}
	}

	public static void closeEventGroup() {
		if (eventLoopGroup != null && !eventLoopGroup.isShutdown()) {
			eventLoopGroup.shutdownGracefully();
		}
		eventLoopGroup = null;
	}

	public int getChildNioEventThreads() {
		return childNioEventThreads;
	}

	public void setChildNioEventThreads(int childNioEventThreads) {
		this.childNioEventThreads = childNioEventThreads;
	}
	/**
     * Gets loadbalance.
     *
     * @return the loadbalance
     */
    public String getLoadbalance() {
        return loadbalance;
    }

    /**
     * Sets loadbalance.
     *
     * @param loadbalance the loadbalance
     */
    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }
    public void setConnectListeners(List<ConnectListener> connectListeners) {
        this.connectListeners = connectListeners;
    }

    public List<ConnectListener> getConnectListeners() {
        return connectListeners;
    }
}