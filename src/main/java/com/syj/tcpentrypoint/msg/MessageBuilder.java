package com.syj.tcpentrypoint.msg;

import com.syj.tcpentrypoint.util.Constants;
import com.syj.tcpentrypoint.util.Constants.HeadKey;

/**
 * 
*  @des    :Message builder tools
 * @author:shenyanjun1
 * @date   :2018-12-14 17:04
 */
public class MessageBuilder {

	/**
	 * @des build RequestMessage
	 * @param topicId
	 * @param productKey
	 * @return
	 */
	public static RequestMessage buildRequest(String topicId,String productKey) {
		
		RequestMessage requestMessage = new RequestMessage();
		requestMessage.getMsgHeader().setMsgType(Constants.REQUEST_MSG);
		requestMessage.getMsgHeader().addHeadKey(HeadKey.topicid, topicId);
		requestMessage.getMsgHeader().addHeadKey(HeadKey.productkey, productKey);
		return requestMessage;

	}

	/**
	 * Build response.
	 *
	 * @param request the request
	 * @return the response message
	 */
	public static ResponseMessage buildResponse(RequestMessage request) {
		ResponseMessage responseMessage = new ResponseMessage(false);
		responseMessage.setMsgHeader(request.getMsgHeader().clone());
		// clone后不可以再修改header的map里的值，会影响到原来对象
		responseMessage.getMsgHeader().setMsgType(Constants.RESPONSE_MSG);
		// responseMessage.getMsgHeader().setCodecType(request.getMsgHeader().getCodecType());
		// responseMessage.getMsgHeader().setProtocolType(request.getMsgHeader().getProtocolType());

		return responseMessage;
	}

	/**
	 * Build response.
	 *
	 * @param header the MessageHeader
	 * @return the response message
	 */
	public static ResponseMessage buildResponse(MessageHeader header) {
		ResponseMessage responseMessage = new ResponseMessage(false);
		responseMessage.setMsgHeader(header.clone());
		// clone后不可以再修改header的map里的值，会影响到原来对象
		responseMessage.getMsgHeader().setMsgType(Constants.RESPONSE_MSG);
		return responseMessage;
	}

	/**
	 * Build heartbeat request.
	 *
	 * @return request message
	 */
	public static RequestMessage buildHeartbeatRequest() {
		RequestMessage requestMessage = new RequestMessage();
		requestMessage.getMsgHeader().setMsgType(Constants.HEARTBEAT_REQUEST_MSG);
		return requestMessage;
	}

	/**
	 * Build heartbeat response.
	 *
	 * @param heartbeat the heartbeat
	 * @return the response message
	 */
	public static ResponseMessage buildHeartbeatResponse(RequestMessage heartbeat) {
		ResponseMessage responseMessage = new ResponseMessage();
		MessageHeader header = responseMessage.getMsgHeader();
		header.setMsgType(Constants.HEARTBEAT_RESPONSE_MSG);
		header.setMsgId(heartbeat.getRequestId());
		responseMessage.setResponse("heartbeat check ok");
//        header.setProtocolType(heartbeat.getProtocolType());
//        header.setCodecType(heartbeat.getMsgHeader().getCodecType());
		// ResponseMessage responseMessage = new ResponseMessage(false);
		// responseMessage.setMsgHeader(heartbeat.getMsgHeader().clone());
		// responseMessage.getMsgHeader().setMsgType(Constants.HEARTBEAT_RESPONSE_MSG);
		return responseMessage;
	}
}