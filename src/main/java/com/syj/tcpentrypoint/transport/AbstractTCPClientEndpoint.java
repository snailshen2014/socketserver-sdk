package com.syj.tcpentrypoint.transport;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.client.MsgFuture;
import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.error.ClientEndpointClosedException;
import com.syj.tcpentrypoint.error.ClientEndpointTimeoutException;
import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.protocol.ProtocolUtil;
import com.syj.tcpentrypoint.util.NetUtils;
import io.netty.buffer.ByteBuf;

/**
 * Title: TCP类型的长连接<br>
 * <p/>
 * Description: 子类可自行实现调用方法<br>
 * <p/>
 */
abstract class AbstractTCPClientEndpoint extends AbstractClientEndpoint {

	/**
	 * slf4j logger
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTCPClientEndpoint.class);

	/**
	 * 请求id计数器（一个Transport一个）
	 */
	private final AtomicInteger requestId = new AtomicInteger();
	private final ConcurrentHashMap<Integer, MsgFuture> futureMap = new ConcurrentHashMap<Integer, MsgFuture>();

	/**
	 * 构造函数
	 *
	 * @param clientTransportConfig 客户端配置
	 */
	protected AbstractTCPClientEndpoint(ClientEndpointConfig clientTransportConfig) {
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
	public ClientEndpointTransport setClientTransportConfig(ClientEndpointConfig clientEndpointConfig) {
		this.clientEndpointConfig = clientEndpointConfig;
		return this;
	}

	/*
	 * handle the Response
	 */
	public void receiveResponse(ResponseMessage msg) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("receiveResponse {}", msg);
		}
		Integer msgId = msg.getRequestId();
		MsgFuture future = futureMap.get(msgId);
		if (future == null) {
			LOGGER.warn("Not found future which msgId is {} when receive response. May be "
					+ "this future have been removed because of timeout", msgId);
			if (msg != null && msg.getMsgBody() != null) {
				msg.getMsgBody().release();
			}
			// throw new RpcException("No such Future maybe have been removed for
			// Timeout..");
		} else {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("receiveResponse set msgId{} to future.", msgId);
			}
			future.setSuccess(msg);
			futureMap.remove(msgId);
		}
	}

	/*
	 * different FutureMap for different Request msg type
	 */
	protected void addFuture(BaseMessage msg, MsgFuture msgFuture) {
		Integer msgId = msg.getMsgHeader().getMsgId();
		this.futureMap.put(msgId, msgFuture);
	}

	/*
	 * check the future map
	 */
	public void checkFutureMap() {
		long current = System.currentTimeMillis();
		Set<Integer> keySet = futureMap.keySet();
		for (Integer msgId : keySet) {
			MsgFuture future = futureMap.get(msgId);
			if (future != null && future.isAsyncCall()) { // 异步调用
				// 当前时间减去初始化时间 大于 超时时间 说明已经超时
				if (current - future.getGenTime() > future.getTimeout()) {
					LOGGER.debug("remove timeout future:{} from the FutureMap", future);
					MsgFuture removedFuture = futureMap.remove(msgId);
					// 防止之前被处理过，这里判断下
					if (!removedFuture.isDone()) {
						removedFuture.setFailure(removedFuture.clientTimeoutException(true));
					}
					removedFuture.releaseIfNeed();
				}
			}
		}
	}

	/**
	 * 连接断开后，已有请求都不再等待
	 */
	public void removeFutureWhenChannelInactive() {
		LOGGER.debug("Interrupt wait of all futures : {} ", futureMap.size());
		Exception e = new ClientEndpointClosedException("Channel " + NetUtils.channelToString(localAddress, remoteAddress)
				+ " has been closed, remove future when channel inactive");
		for (Map.Entry<Integer, MsgFuture> entry : futureMap.entrySet()) {
			MsgFuture future = entry.getValue();
			if (!future.isDone()) {
				future.setFailure(e);
			}
		}
	}

	@Override
	public ResponseMessage send(BaseMessage msg, int timeout) {
		Integer msgId = null;
		try {
			// do some thing
			super.currentRequests.incrementAndGet();
			msgId = genarateRequestId();
			msg.setRequestId(msgId);

			ByteBuf byteBuf = PooledBufHolder.getBuffer();
			byteBuf = ProtocolUtil.encode(msg, byteBuf);
			msg.setMsg(byteBuf);
			MsgFuture<ResponseMessage> future = doSend(msg, timeout);
			 return future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
            throw new RpcException("Client request thread interrupted");
        } catch (ClientEndpointTimeoutException e) {
            try {
                if (msgId != null) {
                    futureMap.remove(msgId);
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
	 * @param msg     消息
	 * @param timeout 超时时间
	 * @return 返回结果Future
	 */
	abstract MsgFuture doSend(BaseMessage msg, int timeout);

	/**
	 * 得到当前Future列表的大小
	 *
	 * @return Future列表的大小
	 */
	public int getFutureMapSize() {
		return futureMap.size();
	}
}