package rdp.gold.brute;

import java.util.Scanner;

public class RDPEntity {
    private String ip;
    private int port;
    private String login;
    private String password;

    public RDPEntity() {
    }

    public RDPEntity(String ip, int port, String login, String password) {
        this.ip = ip;

        this.port = port;

        this.login = login;

        this.password = password;
    }

    @SuppressWarnings("resource")
    public static RDPEntity parse(String rdp) {
        Scanner scanner = new Scanner(rdp).useDelimiter(";");
        Scanner scannerHost = new Scanner(scanner.next()).useDelimiter(":");

        RDPEntity rdpEntity = new RDPEntity();
        rdpEntity.setIp(scannerHost.next());
        rdpEntity.setPort(scannerHost.nextInt());

        if (scanner.hasNext()) {
            rdpEntity.setLogin(scanner.next());
        } else {
            rdpEntity.setLogin("");
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

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return this.ip + ":" + this.port + ";" + this.login + ";" + this.password;
    }
}
