package com.syj.tcpentrypoint.msg;

/**
 * 
*  @des    :The request message
 * @author:shenyanjun1
 * @date   :2018-12-14 17:05
 */
public class RequestMessage extends BaseMessage {
	private long receiveTime; // temp Property for Request receive time
	private String targetAddress; // Remote address

	public RequestMessage(boolean initHeader) {
		super(initHeader);
	}

	public RequestMessage() {
		super(true);
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getTargetAddress() {
		return targetAddress;
	}

	public void setTargetAddress(String targetAddress) {
		this.targetAddress = targetAddress;
	}
}