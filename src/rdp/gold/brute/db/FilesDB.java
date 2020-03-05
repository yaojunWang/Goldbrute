package rdp.gold.brute.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rdp.gold.brute.Config;
import rdp.gold.brute.entity.HostEntity;
import rdp.gold.brute.entity.LoginPasswordEntity;

@SuppressWarnings({ "unchecked", "resource", "rawtypes" })
public class FilesDB {
    private static final Logger logger = Logger.getLogger(FilesDB.class);
    private static Connection dbConnection = null;
    private static List<String> hostList = new ArrayList();
    private static List<String> loginPasswordList = new ArrayList();
    private static List<String> passwordList = new ArrayList();

    public static boolean hasExistsTable(String tableName) throws SQLException {
        boolean tableExists = false;

        ResultSet rset = dbConnection.getMetaData().getTables(null, null, tableName, null);
        if (rset.next()) {
            tableExists = true;
        }
        return tableExists;
    }

    public static void dropTableIfExists(String tableName) throws SQLException {
        Statement stm = dbConnection.createStatement();
        stm.execute("DROP TABLE IF EXISTS " + tableName);
    }

    public static void importIpList(File ipList) throws Exception {
        hostList.clear();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ipList), "UTF-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                hostList.add(line.trim());
            } catch (Exception e) {
                logger.error(e);
            }
        }
        reader.close();
    }

    public static void importIpList(List<String> ipList) {
        hostList.clear();
        hostList.addAll(ipList);
    }

    public static void importIpListH2(File ipList) throws Exception {
        dropTableIfExists("IP_LIST");

        String sql = "CREATE TABLE IP_LIST(id int, host varchar(21))";
        Statement stm = dbConnection.createStatement();
        stm.execute(sql);

        String insert = "INSERT INTO IP_LIST (id, host) VALUES (?, ?);";
        PreparedStatement preparedStatement = dbConnection.prepareStatement(insert);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ipList), "UTF-8"));

        boolean isAddedBath = false;
        int n = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            n++;
            try {
                HostEntity hostEntity = HostEntity.parseHostEntity(line.trim());

                preparedStatement.setInt(1, n);
                preparedStatement.setString(2, hostEntity.toString());
                preparedStatement.addBatch();
                isAddedBath = true;
                if (n % 10000 == 0) {
                    preparedStatement.executeBatch();
                    System.out.println(n);

                    preparedStatement = dbConnection.prepareStatement(insert);
                    isAddedBath = false;
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        reader.close();
        if (isAddedBath) {
            preparedStatement.executeBatch();
        }
        sql = "CREATE INDEX IP_LIST_ID ON IP_LIST(id)";
        dbConnection.createStatement().execute(sql);
    }

    public static void importLoginPasswordList(File loginPasswordfile) throws Exception {
        loginPasswordList.clear();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(loginPasswordfile), "UTF-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                LoginPasswordEntity loginPasswordEntity = LoginPasswordEntity.parseLoginPasswordEntity(line.trim());

                loginPasswordList.add(loginPasswordEntity.toString());
            } catch (Exception e) {
                logger.error(e);
            }
        }
        reader.close();
    }

    public static void importLoginPasswordList(List<String> loginPasswordList) {
        loginPasswordList.clear();
        loginPasswordList.addAll(loginPasswordList);
    }

    public static void importPasswordList(File passwordFile) throws Exception {
        passwordList.clear();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(passwordFile), "UTF-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                passwordList.add(line.trim());
            } catch (Exception e) {
                logger.error(e);
            }
        }
        reader.close();
    }

    public static void importPasswordList(List<String> passwordList) {
        passwordList.clear();
        passwordList.addAll(passwordList);
    }

    public static void importLoginPasswordListH2(File loginPasswordList) throws Exception {
        dropTableIfExists("LOGIN_PASSWORD_LIST");

        String sql = "CREATE TABLE LOGIN_PASSWORD_LIST(id int, login_password varchar(100))";
        Statement stm = dbConnection.createStatement();
        stm.execute(sql);

        String insert = "INSERT INTO LOGIN_PASSWORD_LIST (id, login_password) VALUES (?, ?);";
        PreparedStatement preparedStatement = dbConnection.prepareStatement(insert);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(loginPasswordList), "UTF-8"));

        boolean isAddedBath = false;
        int n = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            n++;
            try {
                LoginPasswordEntity loginPasswordEntity = LoginPasswordEntity.parseLoginPasswordEntity(line.trim());

                preparedStatement.setInt(1, n);
                preparedStatement.setString(2, loginPasswordEntity.toString());
                preparedStatement.addBatch();

                isAddedBath = true;
            } catch (Exception e) {
                logger.error(e);
            }
        }
        reader.close();
        if (isAddedBath) {
            preparedStatement.executeBatch();
        }
        sql = "CREATE INDEX LOGIN_PASSWORD_LIST_ID ON LOGIN_PASSWORD_LIST(id)";
        dbConnection.createStatement().execute(sql);
    }

    public static List<String> getHostListByBetween(int nStart, int nEnd) throws SQLException {
        List<String> tempHostList = new ArrayList();
        for (int i = nStart; i <= nEnd; i++) {
            try {
                tempHostList.add(hostList.get(i - 1));
            } catch (IndexOutOfBoundsException e) {
                logger.debug("size hostList: " + hostList.size());
                logger.error(e);
            }
        }
        return tempHostList;
    }

    public static List<String> getHostListByBetweenH2(int nStart, int nEnd) throws SQLException {
        Statement stm = dbConnection.createStatement();

        ResultSet rs = stm.executeQuery("SELECT * FROM IP_LIST WHERE id BETWEEN " + nStart + " AND " + nEnd);

        List<String> hostList = new ArrayList();
        while (rs.next()) {
            hostList.add(rs.getString("host"));
        }
        return hostList;
    }

    public static String getLoginPasswordByNumber(int n) {
        try {
            return (String) loginPasswordList.get(n - 1);
        } catch (IndexOutOfBoundsException e) {
        }
        return null;
    }

    public static String getPasswordByNumber(int n) {
        try {
            return (String) passwordList.get(n - 1);
        } catch (IndexOutOfBoundsException e) {
        }
        return null;
    }

    public static String getLoginPasswordByNumberH2(int n) throws SQLException {
        Statement stm = dbConnection.createStatement();

        ResultSet rs = stm.executeQuery("SELECT * FROM LOGIN_PASSWORD_LIST WHERE id = " + n);

        return rs.next() ? rs.getString("login_password") : null;
    }

    public static void main(String[] argc) {
        try {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        FilesDB.importIpList(new File(Config.JAR_PATH + "\\file1.txt"));
                        FilesDB.importLoginPasswordList(new File(Config.JAR_PATH + "\\file2.txt"));

                        Statement stm = FilesDB.dbConnection.createStatement();
                        ResultSet rs = stm.executeQuery("SELECT COUNT(*) as cnt FROM IP_LIST");
                        if (rs.next()) {
                            System.out.println(rs.getInt("cnt"));
                        }
                        rs = stm.executeQuery("SELECT * FROM IP_LIST WHERE id BETWEEN 20222 AND 20322");
                        while (rs.next()) {
                            System.out.println(rs.getInt("id") + "_" + rs.getString("host"));
                        }
                        rs = stm.executeQuery("SELECT COUNT(*) as cnt FROM LOGIN_PASSWORD_LIST");
                        if (rs.next()) {
                            System.out.println(rs.getInt("cnt"));
                        }
                        rs = stm.executeQuery("SELECT * FROM LOGIN_PASSWORD_LIST WHERE id BETWEEN 1000 AND 1100");
                        while (rs.next()) {
                            System.out.println(rs.getInt("id") + "_" + rs.getString("login_password"));
                        }
                        for (;;) {
                            Thread.sleep(100L);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
