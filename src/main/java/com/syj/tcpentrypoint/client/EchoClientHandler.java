/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.syj.tcpentrypoint.client;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import com.syj.tcpentrypoint.msg.MessageBuilder;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.protocol.ProtocolUtil;
import com.syj.tcpentrypoint.transport.PooledBufHolder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

	private final ByteBuf firstMessage;
	private final AtomicInteger CALLTIMES = new AtomicInteger(0);
	/**
	 * Creates a client-side handler.
	 */
	public EchoClientHandler() {
		firstMessage = Unpooled.buffer(EchoClient.SIZE);
		firstMessage.writeByte((byte) 0xFF);//magic
		firstMessage.writeInt(35);//sum length
		//header
		firstMessage.writeShort(7);
		firstMessage.writeBytes("topicId".getBytes());
		firstMessage.writeShort(10);
		firstMessage.writeBytes("productKey".getBytes());
		//body
		firstMessage.writeShort(8);
		firstMessage.writeBytes("jsonData".getBytes());
		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(generateRequest());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		int now = CALLTIMES.incrementAndGet();
		System.out.println("read server msg:" +msg + ",times=" + now );
		  if (now >= 100000)
			  return;
		ctx.write(generateRequest());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
	
	private RequestMessage generateRequest() {
		RequestMessage request = MessageBuilder.buildRequest("t123", "pabc");
		ByteBuf body = PooledBufHolder.getBuffer();
		body.writeBytes("Hello world".getBytes());
		request.setMsgBody(body);
		
		ByteBuf byteBuf = PooledBufHolder.getBuffer();
		byteBuf = ProtocolUtil.encode(request, byteBuf);
		System.out.println("msg buffer;" + byteBuf.toString(Charset.defaultCharset()));
		request.setMsg(byteBuf);
		System.out.println("send generated message : " + request);
		return request;
		
		
	}
	
}
