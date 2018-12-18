import com.syj.tcpentrypoint.transport.ServerTransport;
import com.syj.tcpentrypoint.transport.ServerTransportConfig;

public class TestServer {
	public static void main(String[] args) {
		ServerTransportConfig config = new ServerTransportConfig();
		ServerTransport server = new ServerTransport(config);
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
