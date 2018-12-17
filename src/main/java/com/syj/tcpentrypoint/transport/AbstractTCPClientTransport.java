package com.syj.tcpentrypoint.transport;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.config.ClientConfig;
import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.NetUtils;

/**
 * Title: TCP类型的长连接<br>
 * <p/>
 * Description: 子类可自行实现调用方法<br>
 * <p/>
 */
abstract class AbstractTCPClientTransport extends AbstractClientTransport {

	/**
	 * slf4j logger
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTCPClientTransport.class);

	/**
	 * 请求id计数器（一个Transport一个）
	 */
	private final AtomicInteger requestId = new AtomicInteger();

	/**
	 * 构造函数
	 *
	 * @param clientTransportConfig 客户端配置
	 */
	protected AbstractTCPClientTransport(ClientConfig clientTransportConfig) {
		super(clientTransportConfig);
	}

	/*
	 * impl
	 */
	private int genarateRequestId() {

		return requestId.getAndIncrement();
	}

	/**
	 * 设置客户端选项
	 *
	 * @param transportConfig the transport config
	 */
	public ClientTransport setClientTransportConfig(ClientConfig transportConfig) {
		this.clientTransportConfig = transportConfig;
		return this;
	}
	 /*
     *handle the Response
     */
    public void receiveResponse(ResponseMessage msg) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("receiveResponse..{}", msg);
        }
    }
}