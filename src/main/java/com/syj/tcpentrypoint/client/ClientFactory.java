package com.syj.tcpentrypoint.client;

import com.syj.tcpentrypoint.config.ClientConfig;
import com.syj.tcpentrypoint.error.IllegalConfigureException;
import com.syj.tcpentrypoint.util.Constants;

/**
 * Title: 客户端工厂类<br>
 * <p/>
 * Description: 无缓存<br>
 * <p/>
 */
public class ClientFactory {

	/**
	 * 构造Client对象
	 * 
	 * @param consumerConfig
	 *            客户端配置
	 * @return Client对象
	 */
	public static Client getClient(ClientConfig consumerConfig) {
		Client client =  new FailoverClient(consumerConfig);
		return client;
	}
}