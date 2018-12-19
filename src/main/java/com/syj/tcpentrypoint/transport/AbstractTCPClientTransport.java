package com.syj.tcpentrypoint.transport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.config.ClientConfig;
import com.syj.tcpentrypoint.error.ClientTimeoutException;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.protocol.ProtocolUtil;
import io.netty.buffer.ByteBuf;

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
	
	 private final ConcurrentHashMap<Integer, ResponseMessage> responseMap = new ConcurrentHashMap<Integer, ResponseMessage>();
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
            LOGGER.trace("receiveResponse {}", msg);
        }
        responseMap.put(msg.getRequestId(), msg);
    }
    
    public ResponseMessage getMessage(Integer id) {
    	ResponseMessage msg = responseMap.get(id);
    	if (msg != null)
    		responseMap.remove(id);
    	return msg;
    }
    
    @Override
    public ResponseMessage send(BaseMessage msg, int timeout) {
        Integer msgId = null;
        try {
        	//do some thing
            super.currentRequests.incrementAndGet();
            msgId = genarateRequestId();
            msg.setRequestId(msgId);
            
            ByteBuf byteBuf = PooledBufHolder.getBuffer();
    		byteBuf = ProtocolUtil.encode(msg, byteBuf);
    		msg.setMsg(byteBuf);
            return doSend(msg, timeout);
        }  catch (ClientTimeoutException e) {
            try {
                if (msgId != null) {
                	responseMap.remove(msgId);
                }
            } catch (Exception e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            throw e;
        } finally {
            super.currentRequests.decrementAndGet();
        }
    }

    /**
     * 长连接默认的调用方法
     *
     * @param msg
     *         消息
     * @param timeout
     *         超时时间
     * @return 返回结果Future
     */
    abstract ResponseMessage doSend(BaseMessage msg, int timeout);
}