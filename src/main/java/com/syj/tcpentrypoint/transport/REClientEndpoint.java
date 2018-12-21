package com.syj.tcpentrypoint.transport;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.client.MsgFuture;
import com.syj.tcpentrypoint.error.InitErrorException;
import com.syj.tcpentrypoint.msg.BaseMessage;

import io.netty.channel.Channel;

/**
 * Title: 客户端传输层<br>
 * <p/>
 * Description: 包装了netty的channel，集成了批量提交等功能<br>
 * <p/>
 */
public class REClientEndpoint extends AbstractTCPClientEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(REClientEndpoint.class);

	private Channel channel;

	public REClientEndpoint(final Channel channel) {
		super(null);
		setChannel(channel);
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
		super.remoteAddress = channel.remoteAddress();
		super.localAddress = channel.localAddress();
	}

	public Channel getChannel() {
		return this.channel;
	}

	/*
	 * actually do the reconnect logic here
	 */
	public void reconnect() {
		if (!isOpen() && clientEndpointConfig != null) {
			try {
				ClientEndpointFactory.reconn(this);
			} catch (InitErrorException e) {
				logger.debug(e.getMessage(), e);
				throw e;
			}

		} else {
			logger.debug("reconnect bypass for channel status:{} and ClientEndpointConfig:{}.", channel,
					clientEndpointConfig);
		}

	}

	@Override
	public void shutdown() {
		if (channel != null && channel.isOpen()) {
			try {
				channel.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean isOpen() {
		return channel != null && channel.isActive() && channel.isOpen();
	}

	@Override
	public MsgFuture doSend(final BaseMessage msg, int timeout) {
		logger.info("REClientTransport doSend msgId:{},timeout:{}", msg.getRequestId(), timeout);
		final MsgFuture resultFuture = new MsgFuture(getChannel(), msg.getMsgHeader(), timeout);
		this.addFuture(msg, resultFuture);
		channel.writeAndFlush(msg, channel.voidPromise());
		resultFuture.setSentTime(System.currentTimeMillis());// 置为已发送
		return resultFuture;
	}

}