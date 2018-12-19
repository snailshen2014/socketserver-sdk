import com.syj.tcpentrypoint.client.Client;
import com.syj.tcpentrypoint.client.ClientFactory;
import com.syj.tcpentrypoint.config.ClientConfig;
import com.syj.tcpentrypoint.msg.MessageBuilder;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.transport.PooledBufHolder;

import io.netty.buffer.ByteBuf;

public class TestClient {
	public static void main(String[] args) {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setAppName("mytest");
		clientConfig.setServerList("127.0.0.1:18550");
		Client client = null;
		try {
			client = ClientFactory.getClient(clientConfig);
			int count = 0;
			while (count++ < 100) {
				ResponseMessage response = client.sendMsg(generateRequest("topicId998", "syjpro", "Hello world"));
				System.out.println("response message:" + response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (client != null) {
				client.destroy();
				client = null;
			}
		}
		synchronized (TestClient.class) {
			while (true) {
				try {
					TestClient.class.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private static RequestMessage generateRequest(String topicId, String productKey, String data) {
		RequestMessage request = MessageBuilder.buildRequest(topicId, productKey);
		ByteBuf body = PooledBufHolder.getBuffer();
		body.writeBytes(data.getBytes());
		request.setMsgBody(body);

//		ByteBuf byteBuf = PooledBufHolder.getBuffer();
//		byteBuf = ProtocolUtil.encode(request, byteBuf);
//		System.out.println("msg buffer;" + byteBuf.toString(Charset.defaultCharset()));
//		request.setMsg(byteBuf);
//		System.out.println("send generated message : " + request);
		return request;

	}
}
