package com.syj.tcpentrypoint.transport;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.MessageBuilder;
import com.syj.tcpentrypoint.msg.MessageHeader;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.server.BaseServerHandler;
import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.NetUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * @des :server channel handler ,handle the request and do business
 * @author:shenyanjun1
 * @date :2018-12-14 17:17
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

	private ServerTransportConfig transportConfig;
	private BaseServerHandler serverHandler;
//
//    private final List<ConnectListener> connectListeners;

	public ServerChannelHandler(ServerTransportConfig serverTransportConfig) {
		this.transportConfig = serverTransportConfig;
//        this.connectListeners = serverTransportConfig.getConnectListeners();
		serverHandler = new BaseServerHandler(transportConfig);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Channel channel = ctx.channel();
		if (msg instanceof RequestMessage) {
			RequestMessage requestMsg = (RequestMessage) msg;
			if (handleOtherMsg(ctx, requestMsg))
				return;
			serverHandler.handlerRequest(channel, requestMsg);
		} else if (msg instanceof ResponseMessage) {
			// receive the callback ResponseMessage
			ResponseMessage responseMsg = (ResponseMessage) msg;
//            if (responseMsg.getMsgHeader().getMsgType() != Constants.CALLBACK_RESPONSE_MSG) {
//                throw new RpcException(responseMsg.getMsgHeader(), "Can not handle normal response message" +
//                        " in server channel handler : " + responseMsg.toString());
//            }
			// find the transport
//            REClientTransport clientTransport = CallbackUtil.getClientTransport(channel);
//            if (clientTransport != null) {
//                clientTransport.receiveResponse(responseMsg);
//            } else {
//                logger.error("no such clientTransport for channel:{}", channel);
//                throw new RpcException(responseMsg.getMsgHeader(), "No such clientTransport");
//            }
		} else {
			throw new RpcException("Only support base message");
		}

	}

	/*
	 * handle the error
	 */
	public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) {
		Channel channel = ctx.channel();
		if (cause instanceof IOException) {
			logger.warn("catch IOException at {} : {}",
					NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()), cause.getMessage());
		} else if (cause instanceof RpcException) {
			RpcException rpc = (RpcException) cause;
			MessageHeader header = rpc.getMsgHeader();
			if (header != null) {
				ResponseMessage responseMessage = new ResponseMessage();
				responseMessage.getMsgHeader().copyHeader(header);
				responseMessage.getMsgHeader().setMsgType(Constants.RESPONSE_MSG);
				String causeMsg = cause.getMessage();
//                String channelInfo = BaseServerHandler.getKey(ctx.channel());
//                String causeMsg2 = "Remote Error Channel:" + channelInfo + " cause: " + causeMsg;
//                ((RpcException) cause).setErrorMsg(causeMsg2);
//                responseMessage.setException(cause);
				ChannelFuture channelFuture = ctx.writeAndFlush(responseMessage);
				channelFuture.addListener(new ChannelFutureListener() {

//                    @Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							if (logger.isTraceEnabled()) {
								logger.trace("have write the error message back to clientside..");
							}
							return;
						} else {
							logger.error("fail to write error back status: {}", future.isSuccess());

						}
					}
				});
			}
		} else {
			logger.warn("catch " + cause.getClass().getName() + " at {} : {}",
					NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()), cause.getMessage());
		}
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		logger.info("connected from {}", NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()));
//        BaseServerHandler.addChannel(channel);
//        if (connectListeners != null) {
//            serverHandler.getBizThreadPool().execute(new Runnable() {
//                @Override
//                public void run() {
//                    for (ConnectListener connectListener : connectListeners) {
//                        try {
//                            connectListener.connected(ctx);
//                        } catch (Exception e) {
//                            logger.warn("Failed to call connect listener when channel active", e);
//                        }
//                    }
//                }
//            });
//        }
	}

	/**
	 * @des handle other request
	 * @param ctx
	 * @param requestMsg
	 * @return
	 */
	private boolean handleOtherMsg(ChannelHandlerContext ctx, RequestMessage requestMsg) {

		int msgType = requestMsg.getMsgHeader().getMsgType();
		if (msgType == Constants.REQUEST_MSG)
			return false; // 正常的请求
		Channel channel = ctx.channel();
		ResponseMessage response = null;
		switch (msgType) {
		case Constants.HEARTBEAT_REQUEST_MSG:
			response = MessageBuilder.buildHeartbeatResponse(requestMsg);
			break;
		default:
			throw new RpcException(requestMsg.getMsgHeader(), " no such msgType:" + msgType);

		}
		channel.writeAndFlush(response);
		return true;
	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		logger.info("Disconnected from {}", NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()));
//        BaseServerHandler.removeChannel(channel);
//        if (connectListeners != null) {
//            serverHandler.getBizThreadPool().execute(new Runnable() {
//                @Override
//                public void run() {
//                    for (ConnectListener connectListener : connectListeners) {
//                        try {
//                            connectListener.disconnected(ctx);
//                        } catch (Exception e) {
//                            logger.warn("Failed to call connect listener when channel inactive", e);
//                        }
//                    }
//                }
//            });
//        }
//        CallbackUtil.removeTransport(channel);
	}

//    public BaseServerHandler getServerHandler() {
////        return serverHandler;
//    }

}