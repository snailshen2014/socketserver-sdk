import com.syj.tcpentrypoint.transport.ServerEndpoint;
import com.syj.tcpentrypoint.transport.ServerEndpointConfig;

public class TestServer {
	public static void main(String[] args) {
		ServerEndpointConfig config = new ServerEndpointConfig();
		ServerEndpoint server = new ServerEndpoint(config);
		server.start();
		synchronized (TestServer.class) {
			while (true) {
				try {
					TestServer.class.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
