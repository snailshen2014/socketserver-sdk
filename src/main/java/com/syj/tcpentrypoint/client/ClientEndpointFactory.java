package com.syj.tcpentrypoint.client;

import com.syj.tcpentrypoint.config.ClientEndpointConfig;

/**
 * 
*  @des    :客户端工厂类
 * @author:shenyanjun1
 * @date   :2018-12-20 16:51
 */
public class ClientEndpointFactory {

	/**
	 * 构造Client对象
	 * 
	 * @param consumerConfig
	 *            客户端配置
	 * @return Client对象
	 */
	public static ClientEndpoint getClient(ClientEndpointConfig consumerConfig) {
		ClientEndpoint client =  new FailoverClient(consumerConfig);
		return client;
	}
}