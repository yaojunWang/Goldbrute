package rdp.gold.brute.entity;

import java.util.Objects;
import java.util.Scanner;

public class RDPEntity {
    private String ip;
    private int port;
    private String domain;
    private String login;
    private String password;
    private Boolean isValid = Boolean.valueOf(false);

    public RDPEntity() {
    }

    public RDPEntity(String ip, int port, String login, String password) {
        this.ip = ip;

        this.port = port;

        this.login = login;

        this.password = password;
    }

    public RDPEntity(String ip, int port, String domain, String login, String password) {
        this.ip = ip;

        this.port = port;

        this.login = login;

        this.domain = domain;

        this.password = password;
    }

    public RDPEntity(String ip, int port, String login, String password, Boolean isValid) {
        this.ip = ip;

        this.port = port;

        this.login = login;

        this.password = password;

        this.isValid = isValid;
    }

    public static String getIpTemplate(java.net.InetAddress ip) {
        StringBuilder ipTemplate = new StringBuilder();

        int cntOctet = 0;
        for (byte raw : ip.getAddress()) {
            String octet = new Integer(raw & 0xFF).toString();

            cntOctet++;
            int cnt = 0;
            for (String item : octet.split("")) {
                cnt++;

                if ((octet.length() - cnt == 0) && (cntOctet <= 2)) {
                    break;
                }

                ipTemplate.append(item);
            }

            if (cntOctet <= 2) {
                for (int i = 0; i < 4 - cnt; i++) {
                    ipTemplate.append("*");
                }
            }

            if (cntOctet != 4) {
                ipTemplate.append(".");
            }
        }

        return ipTemplate.toString();
    }

    @SuppressWarnings("resource")
    public static RDPEntity parseRDPEntity(String rdp) {
        Scanner scanner = new Scanner(rdp).useDelimiter(";");
        Scanner scannerHost = new Scanner(scanner.next()).useDelimiter(":");

        RDPEntity rdpEntity = new RDPEntity();
        rdpEntity.setIp(scannerHost.next());
        rdpEntity.setPort(scannerHost.nextInt());

        Scanner scannerLogin = new Scanner(scanner.next()).useDelimiter("\\\\");
        String loginDomain = scannerLogin.next();

        if (scannerLogin.hasNext()) {
            rdpEntity.setDomain(loginDomain);
            rdpEntity.setLogin(scannerLogin.next());
        } else {
            rdpEntity.setLogin(loginDomain);
        }

        if (scanner.hasNext()) {
            rdpEntity.setPassword(scanner.next());
        } else {
            rdpEntity.setPassword("");
        }

        return rdpEntity;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getValid() {
        return this.isValid;
    }

    public void setValid(Boolean valid) {
        this.isValid = valid;
    }

    public String toString() {
        return this.ip + ":" + this.port + ";" + this.login + ";" + this.password;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RDPEntity))
            return false;
        RDPEntity rdpEntity = (RDPEntity) o;
        return (getPort() == rdpEntity.getPort()) && (Objects.equals(getIp(), rdpEntity.getIp())) && (Objects.equals(getDomain(), rdpEntity.getDomain()))
                && (Objects.equals(getLogin(), rdpEntity.getLogin())) && (Objects.equals(getPassword(), rdpEntity.getPassword())) && (Objects.equals(this.isValid, rdpEntity.isValid));
    }

    public int hashCode() {
        return Objects.hash(new Object[] { getIp(), Integer.valueOf(getPort()), getDomain(), getLogin(), getPassword(), this.isValid });
    }
}
