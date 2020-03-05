package rdp.gold.brute.synchronizer.packages.write;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.Registry;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.synchronizer.packages.AbstractPackage;
import rdp.gold.brute.synchronizer.packages.WritePackage;
import rdp.gold.brute.synchronizer.packages.read.ProjectConfigPackage;

public class BruteProjectPackage extends AbstractPackage implements WritePackage {
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass());

    public BruteProjectPackage(SynchronizerInfo synchronizerInfoObject) {
        super(synchronizerInfoObject, "BruteProjectPackage");
    }

    public ByteBuffer getPackage() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Config.PROJECT_ID.toString());
        stringBuilder.append(";");
        stringBuilder.append(Config.IS_VALID_SERVER_SCAN.get());
        stringBuilder.append(";");
        stringBuilder.append(Config.IS_VALID_SERVER_BRUTE.get());
        stringBuilder.append(";");
        stringBuilder.append(Registry.IS_LOAD_IP_LOGIN_PASSWORD_LIST);

        return createPacket(stringBuilder.toString().getBytes());
    }

    public Class<?> getAwaitPackage() {
        return ProjectConfigPackage.class;
    }

    public Class<?> getWritePackage() {
        return null;
    }
}
