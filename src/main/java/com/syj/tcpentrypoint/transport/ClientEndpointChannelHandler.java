package com.syj.tcpentrypoint.transport;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.BaseMessage;
import com.syj.tcpentrypoint.msg.MessageBuilder;
import com.syj.tcpentrypoint.msg.MessageHeader;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.util.ExceptionUtils;
import com.syj.tcpentrypoint.util.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Title: 调用端channel处理器<br>
 * <p/>
 * Description: 处理建立/断开连接，收到数据等事件<br>
 * <p/>
 */
public class ClientEndpointChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientEndpointChannelHandler.class);

    private REClientEndpoint clientTransport;
    public ClientEndpointChannelHandler(){
        
    }

    public ClientEndpointChannelHandler(REClientEndpoint clientTransport) {
        this.clientTransport = clientTransport;
       
    }

    public ClientEndpointChannelHandler(ClientEndpointConfig clientconfig) {
        
    }

    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
   
  
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.info("Channel inactive: {}", channel);
//        clientTransport.removeFutureWhenChannelInactive(); // 结束已有请求
     
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        try {
            if(msg instanceof ResponseMessage){
                ResponseMessage responseMessage = (ResponseMessage) msg;
                clientTransport.receiveResponse(responseMessage);

            } else if (msg instanceof RequestMessage) {
                RequestMessage request = (RequestMessage) msg;
                if(request.isHeartBeat()) {
                    ResponseMessage response = MessageBuilder.buildHeartbeatResponse(request);
                    channel.writeAndFlush(response);
                }  else {
                    throw new RpcException(request.getMsgHeader(), "Should receive callback msg in channel "
                            + NetUtils.channelToString(channel.localAddress(), channel.remoteAddress())
                            + "! " + request.toString());
                }

            } else if (msg instanceof BaseMessage) {
                BaseMessage base = (BaseMessage) msg;
                if (logger.isTraceEnabled()) {
                    logger.trace("msg id:{},msg type:{}", base.getMsgHeader().getMsgId(), base.getMsgHeader().getMsgType());
                }
                throw new RpcException(base.getMsgHeader(), "error type of BaseMessage...");
            } else {
                logger.error("not a type of CustomMsg ...:{} ",msg);
                throw new RpcException("error type..");
                //ctx.
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            BaseMessage base = (BaseMessage)msg;
            MessageHeader header = base != null? base.getMsgHeader():null;
            RpcException rpcException = ExceptionUtils.handlerException(header,e);
            throw rpcException;
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info("event triggered:{}",evt);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        if (cause instanceof IOException) {
            logger.warn("catch IOException at {} : {}",
                    NetUtils.channelToString(channel.localAddress(), channel.remoteAddress()),
                    cause.getMessage());
        } else if (cause instanceof RpcException) {
            RpcException rpcException = (RpcException) cause;
            logger.warn("catch " + cause.getClass().getName() + " at {} : {}",
                    NetUtils.channelToString(channel.localAddress(), channel.remoteAddress()),
                    cause.getMessage());
        } else {
            logger.warn("catch " + cause.getClass().getName() + " at {} : {}",
                    NetUtils.channelToString(channel.localAddress(), channel.remoteAddress()),
                    cause.getMessage());
        }
    }

    public void setClientTransport(REClientEndpoint transport){
        this.clientTransport = transport;
    }
}