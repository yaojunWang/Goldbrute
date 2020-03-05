package rdp.gold.brute.synchronizer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import rdp.gold.brute.synchronizer.packages.ReadPackage;
import rdp.gold.brute.synchronizer.packages.WritePackage;
import rdp.gold.brute.synchronizer.packages.read.ServerAuthorizationPackage;

@WebSocket(maxBinaryMessageSize = 1048576)
public class SynchronizerSocket extends Thread {
    private final Logger logger = Logger.getLogger(SynchronizerSocket.class);

    private Session session;

    private RemoteEndpoint remote;

    private Class<?> awaitReadPackage;
    private SynchronizerInfo synchronizerInfo = new SynchronizerInfo();

    private CountDownLatch connectAwait = new CountDownLatch(1);

    public SynchronizerSocket() {
        this.awaitReadPackage = ServerAuthorizationPackage.class;
    }

    public void run() {
        try {
            this.connectAwait.await(11000L, TimeUnit.MILLISECONDS);

            if (this.session != null) {
                this.logger.info("Connected");
            }

            while ((this.session != null) && (this.session.isOpen())) {
                Thread.sleep(1000L);
            }

            this.logger.info("Remove SynchronizerSocket");
        } catch (InterruptedException localInterruptedException) {
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session sessionJetty) {
        this.logger.info("onConnect");

        this.session = sessionJetty;
        this.remote = this.session.getRemote();

        this.connectAwait.countDown();
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        this.logger.info("onMessage: " + message);
    }

    @OnWebSocketMessage
    public void onMessage(byte[] bytes, int offset, int length) throws Exception {
        try {
            if (this.awaitReadPackage != null) {
                Constructor<?> constructorReadPackage = this.awaitReadPackage.getConstructor(new Class[] { SynchronizerInfo.class });
                ReadPackage packageRead = (ReadPackage) constructorReadPackage.newInstance(new Object[] { this.synchronizerInfo });
                packageRead.processPacket(bytes);

                this.logger.info("Read package: " + this.awaitReadPackage.getName());

                if (packageRead.getWritePackage() != null) {
                    writePackage(packageRead.getWritePackage());
                } else {
                    this.awaitReadPackage = packageRead.getAwaitPackage();

                    if (this.awaitReadPackage != null) {
                        this.logger.info("Await package: " + this.awaitReadPackage.getName());
                    } else {
                        this.logger.info("Close connect");
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.logger.error(e + " " + sw);

            throw e;
        }
    }

    private void writePackage(Class<?> classPackageWrite) throws Exception {
        Constructor<?> constructorWritePackage = classPackageWrite.getConstructor(new Class[] { SynchronizerInfo.class });
        WritePackage packageWrite = (WritePackage) constructorWritePackage.newInstance(new Object[] { this.synchronizerInfo });
        ByteBuffer byteBuffer = packageWrite.getPackage();

        this.remote.sendBytes(byteBuffer);

        this.logger.info("Write package: " + packageWrite.getClass().getName());

        if (packageWrite.getWritePackage() != null) {
            writePackage(packageWrite.getWritePackage());
        } else {
            this.awaitReadPackage = packageWrite.getAwaitPackage();

            if (this.awaitReadPackage != null) {
                this.logger.info("Await package: " + this.awaitReadPackage.getName());
            } else {
                this.logger.info("Close connect");
            }
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.logger.info("Close: statusCode=" + statusCode + ", reason=" + reason);
        this.session.close();
    }
}
