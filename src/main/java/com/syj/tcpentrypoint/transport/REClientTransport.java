package com.syj.tcpentrypoint.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.error.InitErrorException;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;

import io.netty.channel.Channel;

/**
 * Title: 客户端传输层<br>
 * <p/>
 * Description: 包装了netty的channel，集成了批量提交等功能<br>
 * <p/>
 */
public class REClientTransport extends AbstractTCPClientTransport {

	private static final Logger logger = LoggerFactory.getLogger(REClientTransport.class);

	private Channel channel;

	public REClientTransport(final Channel channel) {
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
		if (!isOpen() && clientTransportConfig != null) {
			try {
				ClientTransportFactory.reconn(this);
			} catch (InitErrorException e) {
				logger.debug(e.getMessage(), e);
				throw e;
			}

		} else {
			logger.debug("reconnect bypass for channel status:{} and ClientTransportConfig:{}.", channel,
					clientTransportConfig);
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
	public ResponseMessage send(BaseMessage msg, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

}