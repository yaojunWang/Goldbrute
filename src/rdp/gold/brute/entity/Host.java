package rdp.gold.brute.entity;

public class Host {
    private String ip;
    private int port;
    private Boolean isBrutable;

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

    public Boolean getBrutable() {
        return this.isBrutable;
    }

    public void setBrutable(Boolean brutable) {
        this.isBrutable = brutable;
    }

    public String toString() {
        return "Host{ip='" + this.ip + '\'' + ", port=" + this.port + ", isBrutable=" + this.isBrutable + '}';
    }
}
