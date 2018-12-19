package com.syj.tcpentrypoint.server;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.msg.MessageBuilder;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.protocol.ProtocolUtil;
import com.syj.tcpentrypoint.transport.PooledBufHolder;
import com.syj.tcpentrypoint.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

/**
 * 
*  @des    :The business task for calling the rulesEngine
 * @author:shenyanjun1
 * @date   :2018-12-14 17:13
 */
public class RETask implements Runnable {
	private final Channel channel;
	private final RequestMessage msg;
	private final static Logger logger = LoggerFactory.getLogger(RETask.class);

	public RETask(final RequestMessage msg, Channel channel) {
		this.msg = msg;
		this.channel = channel;
	}

	@Override
	public void run() {
		try {
			ResponseMessage responseMessage = invoke(this.msg);
			logger.debug("Invoker call and Response:{}", responseMessage);
			Future channelFuture = channel.writeAndFlush(responseMessage);
			channelFuture.addListener(new FutureListener() {
				@Override
				public void operationComplete(Future future) throws Exception {
					if (future.isSuccess()) {
						if (logger.isTraceEnabled()) {
							logger.trace("have write the  message back to clientside..");
						}
					} else {
						Throwable throwable = future.cause();
						logger.error("Failed to send error to remote  for msg id: " + " Cause by:", throwable);
					}
				}
			});

		} finally {
			 //release the byteBuf here
            ByteBuf byteBuf = msg.getMsgBody();
            if (byteBuf != null) {
                byteBuf.release();
            }
            if (msg.getMsg() != null) {
            	msg.getMsg().release();
            }
		}

	}

	private ResponseMessage invoke(final RequestMessage msg)  {
		//parse header
		String topicId = (String) msg.getMsgHeader().getAttrByKey(Constants.HeadKey.topicid); 
        String productKey = (String) msg.getMsgHeader().getAttrByKey(Constants.HeadKey.productkey); 
		//parse body
        String jsonData =  msg.getMsgBody().toString(Charset.defaultCharset());
        //call rulesengine
        logger.info("Call rulesEngine ,topicId={},productKey={},jsonData={}.",topicId,productKey,jsonData);
		ResponseMessage response = MessageBuilder.buildResponse(msg);
		//if call rulesengine exception
//		response.setException(new RpcException(ExceptionUtils.toString(exception)));
		response.setResponse("rules execute ok.");
		
		ByteBuf buf = PooledBufHolder.getBuffer();
		buf = ProtocolUtil.encode(response, buf);
		response.setMsg(buf);
		return  response;
	}
}
