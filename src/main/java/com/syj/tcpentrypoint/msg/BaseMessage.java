package com.syj.tcpentrypoint.msg;

import com.syj.tcpentrypoint.util.Constants;
import io.netty.buffer.ByteBuf;
/**
 * 
*  @des    :The base message define of the framework
 * @author:shenyanjun1
 * @date   :2018-12-14 17:04
 */

public abstract class BaseMessage {

    private  MessageHeader msgHeader;

    //head message byte represent
    private ByteBuf msg;

    private ByteBuf msgBody;    //just json data body..

    public ByteBuf getMsg() {
        return msg;
    }

    public void setMsg(ByteBuf msg) {
        this.msg = msg;
    }
    protected BaseMessage(boolean initHeader) {
        if (initHeader) {
            msgHeader = new MessageHeader();
        }
    }

    public MessageHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MessageHeader msgHeader) {
        this.msgHeader = msgHeader;
    }

    public int getRequestId() {
		return msgHeader != null ? msgHeader.getMsgId() : -1;
	}

	/**
	 * @param msgId
	 */
	public void setRequestId(Integer msgId) {

		msgHeader.setMsgId(msgId);
	}

    public boolean isHeartBeat() {
        int msgType = msgHeader.getMsgType();
        return msgType == Constants.HEARTBEAT_REQUEST_MSG
                || msgType == Constants.HEARTBEAT_RESPONSE_MSG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseMessage)) return false;

        BaseMessage that = (BaseMessage) o;

        if (!msgHeader.equals(that.msgHeader)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return msgHeader.hashCode();
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "msgHeader=" + msgHeader +
                '}';
    }

    public ByteBuf getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(ByteBuf msgBody) {
        this.msgBody = msgBody;
    }
}