package com.syj.tcpentrypoint.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;

/**
 * 
*  @des    :失败重试
 * @author:shenyanjun1
 * @date   :2018-12-20 16:52
 */
public class FailoverClient extends ClientEndpoint {

	/**
	 * slf4j logger for this class
	 */
	private final static Logger LOGGER = LoggerFactory
			.getLogger(FailoverClient.class);

	/**
	 * @param consumerConfig ConsumerConfig
	 */
	public FailoverClient(ClientEndpointConfig consumerConfig) {
		super(consumerConfig);
	}

	@Override
	public ResponseMessage doSendMsg(RequestMessage msg) {
		int time = 0;
        Throwable throwable = null;// 异常日志
        int retries = 1;
        ResponseMessage result = null;
        do {
            Connection connection = super.select(msg, null);
			try {
				result = super.sendMsg0(connection, msg);
                if (result != null) {
                    if (throwable != null) {
                        LOGGER.warn("Although success by retry, last exception is: {}", throwable.getMessage());
                    }
                   	break;
                } else {
                    throwable = new RpcException("Failed to call "+ " on remote server " + connection.getEndPoint() + ", return null");
                    time++;
                }
            } catch (RpcException e) { // rpc异常重试
                throwable = e;
                time++;
			} catch (Exception e) { // 其它异常不重试
                throw new RpcException("Failed to call "+ " on remote server: " + connection.getEndPoint() + ", cause by unknown exception: "
                        + e.getClass().getName() + ", message is: " + e.getMessage(), e);
            }
          
		} while (time < retries);
        return result;
	}
}