package com.syj.tcpentrypoint.protocol;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.error.RECodecException;
import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.msg.MessageHeader;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.util.CodecUtils;
import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.ExceptionUtils;

import io.netty.buffer.ByteBuf;

/**
 * 
*  @des    :The REProtocol encoder ,decoder tool
 * @author:shenyanjun1
 * @date   :2018-12-14 17:45
 */
public class ProtocolUtil {

	private static final Logger logger = LoggerFactory.getLogger(ProtocolUtil.class);

	public static ByteBuf encode(Object obj, ByteBuf buffer) {
		if (logger.isTraceEnabled()) {
			logger.trace("readable byte here:{}", buffer.readableBytes());
		}
		MessageHeader header = null;
		try {
			if (obj instanceof RequestMessage) {
				RequestMessage request = (RequestMessage) obj;
				MessageHeader msgHeader = request.getMsgHeader();
				msgHeader.setMsgType(request.getMsgHeader().getMsgType());

				ByteBuf body = request.getMsgBody();
				if (body != null) {
					request.getMsgHeader().setHeaderLength(CodecUtils.encodeHeader(msgHeader, buffer)); // header
					buffer = buffer.writeBytes(body); // body 
					request.getMsgHeader().setLength(buffer.readableBytes() + 1 + 4);
				} else {
					CodecUtils.encodeHeader(msgHeader, buffer);// header only
				}
			} else if (obj instanceof ResponseMessage) {
				ResponseMessage response = (ResponseMessage) obj;
				MessageHeader msgHeader = response.getMsgHeader();
				response.getMsgHeader().setHeaderLength(CodecUtils.encodeHeader(msgHeader, buffer)); // header
				buffer = buffer.writeBytes(response.getResponse().toString().getBytes()); // body
				response.getMsgHeader().setLength(buffer.readableBytes());

			} else {
				throw new RECodecException("no such kind of  message..");

			}
			return buffer;

		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			RpcException rException = ExceptionUtils.handlerException(header, e);
			throw rException;
		}
	}

	public static BaseMessage decode(ByteBuf byteBuf) {
		MessageHeader header = null;
		Integer msgLength = byteBuf.readableBytes() + 5;// magiccode + msg length(4 byte)
		BaseMessage msg = null;
		try {
			Short headerLength = byteBuf.readShort();
			header = CodecUtils.decodeHeader(byteBuf, headerLength);
			header.setHeaderLength(headerLength);
			header.setLength(msgLength);
			msg = enclosure(byteBuf, header);

		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			RpcException rpcException = ExceptionUtils.handlerException(header, e);
			byteBuf.release();// release the byteBuf when decode hit on error
			throw rpcException;
		}
		return msg;

	}

	public static BaseMessage enclosure(ByteBuf byteBuf, MessageHeader header) {
		int msgType = header.getMsgType();
		BaseMessage msg = null;
		try {
			switch (msgType) {
			case Constants.REQUEST_MSG:
				RequestMessage tmp = new RequestMessage();
				tmp.setReceiveTime(System.currentTimeMillis());
				tmp.setMsgBody(byteBuf.slice(byteBuf.readerIndex(), byteBuf.readableBytes()));
				msg = tmp;
				break;
			case Constants.RESPONSE_MSG:
				ResponseMessage response = new ResponseMessage();
				 response.setMsgBody(byteBuf.slice(byteBuf.readerIndex(), byteBuf.readableBytes()));
				 response.setResponse(byteBuf.slice(byteBuf.readerIndex(), byteBuf.readableBytes()).toString(Charset.defaultCharset()));
				 msg = response;
				break;
			case Constants.HEARTBEAT_REQUEST_MSG:
				msg = new RequestMessage();
				byteBuf.release();
				break;
			case Constants.HEARTBEAT_RESPONSE_MSG:
				msg = new ResponseMessage();
				byteBuf.release();
				break;
			default:
				throw new RpcException(header, "Unknown message type in header!");
			}
			if (msg != null) {
				msg.setMsgHeader(header);
			}
		} catch (Exception e) {
            RpcException rException = ExceptionUtils.handlerException(header,e);
            throw rException;
		}
		return msg;

	}

	private static String bytetoString(ByteBuf buf) {
		return buf.toString(Charset.defaultCharset());
	}
}