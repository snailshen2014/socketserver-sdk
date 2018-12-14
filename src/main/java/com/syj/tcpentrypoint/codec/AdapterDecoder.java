package com.syj.tcpentrypoint.codec;


import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.re.REDecoder;
import com.syj.tcpentrypoint.re.REEncoder;
import com.syj.tcpentrypoint.transport.ServerChannelHandler;
import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.NetUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 
*  @des    :This framework is designed multi-protocol,so the first decoder 
*  parser the special protocol,now system only support REProtocol
 * @author:shenyanjun1
 * @date   :2018-12-14 16:54
 */
public class AdapterDecoder extends ByteToMessageDecoder {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AdapterDecoder.class);

    public AdapterDecoder(ServerChannelHandler serverChannelHandler, int payload) {
        this.serverChannelHandler = serverChannelHandler;
        this.payload = payload;
    
    }

    private final ServerChannelHandler serverChannelHandler;
    
    //max data 
    int payload;
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 1) {
            return;
        }
        Short magiccode_high = in.getUnsignedByte(0);
        byte b1 = magiccode_high.byteValue();
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();

        // re protocol
        if (isRE(b1)) {
            LOGGER.info("Accept re connection {}", NetUtils.connectToString(remoteAddress, localAddress));
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast(new REDecoder(this.payload));
            pipeline.addLast(new REEncoder());
            pipeline.addLast(serverChannelHandler);
            pipeline.remove(this);
            pipeline.fireChannelActive(); 
        } else {
            LOGGER.info("Accept other protocol connection {}", NetUtils.connectToString(remoteAddress, localAddress));
            ctx.channel().writeAndFlush("Sorry! Not support this protocol.");
            ctx.channel().close();
        }
    }

    private boolean isRE(short magic1) {
        return magic1 == Constants.MAGICCODEBYTE;
    }

}