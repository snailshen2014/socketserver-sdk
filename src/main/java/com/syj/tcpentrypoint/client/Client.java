package com.syj.tcpentrypoint.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.msg.MessageBuilder;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.transport.PooledBufHolder;
import io.netty.buffer.ByteBuf;

/**
 * 
*  @des    :a singleton client for transpond data to server
 * @author:shenyanjun1
 * @date   :2018-12-21 15:46
 */
public class Client {
	//external system using client
	private static volatile Client client;
	//the client
	private static ClientEndpoint clientEndpoint;
	//client config
	private static ClientEndpointConfig config;
	//logger
	private final static Logger LOGGER = LoggerFactory.getLogger(ClientEndpoint.class);

	private Client(ClientEndpointConfig config) {
		try {
			clientEndpoint = ClientFactory.getClient(config);
			this.config = config; 
		} catch (Exception e) {
			LOGGER.error("Instance client error:{}",e.getMessage());
			if (clientEndpoint != null) {
				clientEndpoint.destroy();
				clientEndpoint = null;
			}
				
		}
	}

	/**
	 * @des get a client instance
	 * @param entryName ,entrypoint application's name
	 * @param serverList server endpoint list,for "ip1:port1;ip2:port2"
	 * @return
	 */
	public static Client getClient(String entryName,String serverList) {
		if (client != null)
			return client;
		synchronized (Client.class) {
			if (client == null) {
				ClientEndpointConfig tmp = new ClientEndpointConfig();
				tmp.setAppName(entryName);
				tmp.setServerList(serverList);
				client = new Client(tmp);
			}
		}
		return client;
	}
	/**
	 * @des send data to server
	 * @param topicId
	 * @param productKey
	 * @param data :json data represent
	 * @return
	 */
	public  String sendMessage(String topicId, String productKey, String data) {
		ResponseMessage response  = null;
		try {
			response = clientEndpoint.sendMsg(generateRequest(topicId, productKey, data));
			LOGGER.info("Client appName:{},send a message to server ,the response:{}.",
							config.getAppName(),response.getResponse());
		} catch (Exception e) {
			LOGGER.error("Client appName:{},send a message to server error :{}.",
					config.getAppName(),e.getMessage());
		}
		return (String)response.getResponse();
	}

	/**
	 * @des encoder a requestMessage
	 * @param topicId
	 * @param productKey
	 * @param data
	 * @return
	 */
	private static RequestMessage generateRequest(String topicId, String productKey, String data) {
		RequestMessage request = MessageBuilder.buildRequest(topicId, productKey);
		ByteBuf body = PooledBufHolder.getBuffer();
		body.writeBytes(data.getBytes());
		request.setMsgBody(body);
		return request;
	}

}
