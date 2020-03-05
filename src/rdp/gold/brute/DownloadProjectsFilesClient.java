package rdp.gold.brute;

import java.io.File;
import java.util.Random;

import org.apache.log4j.Logger;

import rdp.gold.brute.db.FilesDB;

public class DownloadProjectsFilesClient extends Thread {
    private static final Logger logger = Logger.getLogger(DownloadProjectsFilesClient.class);

    private static DownloadProjectsFilesClient currentDownload = null;

    public void run() {
        currentDownload = this;

        Registry.IS_RUN_PROCESS_LOAD_IP_LOGIN_PASSWORD_LIST = true;
        // Integer localInteger1 =
        // Registry.CNT_ATTEMPTS_DOWNLOAD_IP_LOGIN_PASSWORD_LIST;
        // Integer localInteger2 = Registry.CNT_ATTEMPTS_DOWNLOAD_IP_LOGIN_PASSWORD_LIST
        // =
        // Integer.valueOf(Registry.CNT_ATTEMPTS_DOWNLOAD_IP_LOGIN_PASSWORD_LIST.intValue()
        // + 1);
        try {
            /*
             * if (!Config.HOST_ADMIN.equals("127.0.0.1")) { Random rand = new Random(); int
             * sleep = rand.nextInt(5000) * 60;
             * 
             * Thread.sleep(sleep); }
             * 
             * File ipLoginPasswordList = new File(Config.JAR_PATH + "\\hash.zip");
             * 
             * BufferedInputStream in = new BufferedInputStream(new
             * java.net.URL(Registry.IP_LOGIN_PASSWORD_LIST_URL).openStream());
             * FileOutputStream fileOutputStream = new
             * FileOutputStream(ipLoginPasswordList); byte[] dataBuffer = new byte['Ѐ']; int
             * bytesRead; while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
             * fileOutputStream.write(dataBuffer, 0, bytesRead); }
             * 
             * fileOutputStream.flush(); fileOutputStream.close();
             */
            // if (rdp.gold.brute.utils.ChecksumFile.checksumMappedFile(ipLoginPasswordList)
            // == Registry.IP_LOGIN_PASSWORD_LIST_FILE_CRS32.longValue()) {
            // ZipFile zipFile = new ZipFile(ipLoginPasswordList.getPath(),
            // Config.FILE_SERVER_PASSWORD.toCharArray());
            // zipFile.extractAll(Config.JAR_PATH);

            FilesDB.importIpList(new File(Config.JAR_PATH + "/src/iplist.txt"));

            File fileLoginPasswords = new File(Config.JAR_PATH + "/src/loginpassword.txt");
            if (fileLoginPasswords.exists()) {
                FilesDB.importLoginPasswordList(fileLoginPasswords);
            }

            File passwords = new File(Config.JAR_PATH + "/src/password.txt");
            if (passwords.exists()) {
                FilesDB.importPasswordList(passwords);
            }

            Registry.IS_LOAD_IP_LOGIN_PASSWORD_LIST = true;
            // }
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));

            logger.error(e + " " + sw + "\r\n" + Registry.IP_LOGIN_PASSWORD_LIST_URL + "__" + Registry.IP_LOGIN_PASSWORD_LIST_FILE_CRS32 + "__" + Registry.IS_LOAD_IP_LOGIN_PASSWORD_LIST + "__"
                    + Registry.IS_RUN_PROCESS_LOAD_IP_LOGIN_PASSWORD_LIST);
        } finally {
            currentDownload = null;
            Registry.IS_RUN_PROCESS_LOAD_IP_LOGIN_PASSWORD_LIST = false;
        }
    }

    public static void interruptDownload() {
        try {
            if (currentDownload != null) {
                currentDownload.interrupt();
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void main(String[] argc) throws Exception {
        Random rand = new Random();
        int sleep = rand.nextInt(5000) * 60;

        System.out.println("Timeout: " + sleep / 1000);

        Thread.sleep(sleep);

        System.out.println("End timeout");
    }
}
