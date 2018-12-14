
package com.syj.tcpentrypoint.re;

import java.nio.charset.Charset;

import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;

import com.syj.tcpentrypoint.error.RECodecException;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.protocol.ProtocolUtil;
import com.syj.tcpentrypoint.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
/**
 * 
*  @des    :The encoder of the REProtocol
 * @author:shenyanjun1
 * @date   :2018-12-14 17:07
 */
public class REEncoder extends MessageToByteEncoder {

	private final static Logger logger = LoggerFactory.getLogger(REEncoder.class);

	@Override
	public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		ByteBuf headBody = null;
		if (out == null) {
			// logger.debug("ByteBuf out is null..");
			out = ctx.alloc().buffer();
		}
		try {
			if (msg instanceof BaseMessage) {
				BaseMessage base = (BaseMessage) msg;
				if (base.getMsg() != null) {
					write(base.getMsg(), out);
					base.getMsg().release();
				} else {
					headBody = ctx.alloc().heapBuffer();
					ProtocolUtil.encode(msg, headBody);
					write(headBody, out);
				}

			} else {
				throw new RECodecException("Not support this type of Object.");
			}

		} finally {
			if (headBody != null)
				headBody.release();
		}

	}
	/**
	 * @des write the data to out channel by REProtocol
	 * @param data
	 * @param out
	 */
	private void write(ByteBuf data, ByteBuf out) {
		int totalLength = 1 + 4 + data.readableBytes();
		if (out.capacity() < totalLength)
			out.capacity(totalLength);
		out.writeByte(Constants.MAGICCODEBYTE); // 写入magiccode
		int length = totalLength - 1; // data.readableBytes() + 4 (4指的是FULLLENGTH)
		out.writeInt(length); // 4 for Length Field
		out.writeBytes(data, data.readerIndex(), data.readableBytes());
		logger.trace("out length:{},content:{}",out.readableBytes(),out.toString(Charset.defaultCharset()));
	}
}