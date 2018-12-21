import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import com.syj.tcpentrypoint.client.Client;
import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.msg.MessageBuilder;
import com.syj.tcpentrypoint.msg.RequestMessage;
import com.syj.tcpentrypoint.transport.PooledBufHolder;
import com.syj.tcpentrypoint.util.ThreadPoolUtils;

import io.netty.buffer.ByteBuf;

public class TestClient {
	public static void main(String[] args) {
		ThreadPoolExecutor executor = ThreadPoolUtils.newCachedThreadPool(100, 120, new LinkedBlockingQueue<Runnable>());
		int count = 0;
		AtomicInteger send = new AtomicInteger(0);
		while (count++  < 10000) {
			executor.submit( new Runnable() {
				public void run() {
					String response = Client.getClient("testClient","127.0.0.1:18550").sendMessage("uyh879", "pro87a", "hello world");
					System.out.println("Thread id: " + Thread.currentThread().getId() + " sended :" + send.incrementAndGet() + 
							"response:" + response);
				}
			});
		}
		System.out.println("Finished send ,count:" + send.incrementAndGet());
		
		synchronized (TestClient.class) {
			while (true) {
				try {
					TestClient.class.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}

