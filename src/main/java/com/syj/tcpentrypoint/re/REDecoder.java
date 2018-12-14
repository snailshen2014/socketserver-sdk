
package com.syj.tcpentrypoint.re;

import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;

import com.syj.tcpentrypoint.codec.LengthFieldBasedFrameDecoder;
import com.syj.tcpentrypoint.protocol.ProtocolUtil;

import io.netty.buffer.ByteBuf;

/**
 * 
 * @des :the decoder of the REProtocol
 * @author:shenyanjun1
 * @date :2018-12-14 17:06
 */
public class REDecoder extends LengthFieldBasedFrameDecoder {

	private static final Logger logger = LoggerFactory.getLogger(REDecoder.class);

	public REDecoder(int maxFrameLength) {
		/*
		 * int maxFrameLength, 最大值 int lengthFieldOffset, 魔术位1B，然后是长度4B，所以偏移：1 int
		 * lengthFieldLength, 总长度占4B，所以长度是：4 int lengthAdjustment,
		 * 总长度的值包括自己，剩下的长度=总长度-4B 所以调整值是：-4 int initialBytesToStrip
		 * 前面5位不用再读取了，可以跳过，所以跳过的值是：5
		 */
		super(maxFrameLength, 1, 4, -4, 5);
	}

	@Override
	public Object decodeFrame(ByteBuf frame) {
		Object result = ProtocolUtil.decode(frame);
		if (logger.isTraceEnabled()) {
			logger.trace("decoder result:{}", result);
		}
		return result;
	}
}