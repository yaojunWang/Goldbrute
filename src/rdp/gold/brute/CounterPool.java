package rdp.gold.brute;

import java.util.concurrent.atomic.AtomicLong;

public class CounterPool {
    public static AtomicLong pool = new AtomicLong(0L);
    public static AtomicLong poolUsed = new AtomicLong(0L);
    public static AtomicLong scannedPorts = new AtomicLong(0L);
    public static AtomicLong scannedIps = new AtomicLong(0L);
    public static AtomicLong notSupportedRdp = new AtomicLong(0L);
    public static AtomicLong useBytes = new AtomicLong(0L);
    public static AtomicLong countNotValidRdp = new AtomicLong(0L);
    public static AtomicLong countCheckedCombinations = new AtomicLong(0L);
    public static AtomicLong countCheckedIp = new AtomicLong(0L);
    public static AtomicLong countValid = new AtomicLong(0L);
    public static AtomicLong countX224RDP = new AtomicLong(0L);
    public static AtomicLong countCredSSPRDP = new AtomicLong(0L);
    public static AtomicLong countScanQueue = new AtomicLong(0L);
    public static AtomicLong countBruteQueue = new AtomicLong(0L);

    public static void reset() {
        pool.set(0L);
        scannedPorts.set(0L);
        scannedIps.set(0L);
        notSupportedRdp.set(0L);
        useBytes.set(0L);
        countNotValidRdp.set(0L);
        countCheckedCombinations.set(0L);
        countCheckedIp.set(0L);
        countValid.set(0L);

        countX224RDP.set(0L);

        countCredSSPRDP.set(0L);

        countScanQueue.set(0L);

        countBruteQueue.set(0L);
    }
}
