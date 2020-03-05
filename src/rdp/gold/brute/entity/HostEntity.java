package rdp.gold.brute.entity;

import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class HostEntity {
    public static enum TYPE {
        OPEN_PORT, X224, CREDSSP, SSH;
        private TYPE() {
        }
    }

    public static final Map<TYPE, Integer> mapTypeInteger = new java.util.HashMap();
    private String ip;

    static {
        mapTypeInteger.put(TYPE.OPEN_PORT, Integer.valueOf(1));
        mapTypeInteger.put(TYPE.X224, Integer.valueOf(2));
        mapTypeInteger.put(TYPE.CREDSSP, Integer.valueOf(3));
        mapTypeInteger.put(TYPE.SSH, Integer.valueOf(4));

        java.util.Collections.unmodifiableMap(mapTypeInteger);
    }

    private int port;

    public HostEntity(String ip, int port) {
        this.ip = ip;
        this.port = port;
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

    @SuppressWarnings("resource")
    public static HostEntity parseHostEntity(String host) throws Exception {
        java.util.Scanner scannerHost = new java.util.Scanner(host).useDelimiter(":");

        HostEntity hostEntity = new HostEntity();
        hostEntity.setIp(scannerHost.next());
        hostEntity.setPort(scannerHost.nextInt());

        return hostEntity;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HostEntity))
            return false;
        HostEntity that = (HostEntity) o;
        return (getPort() == that.getPort()) && (java.util.Objects.equals(getIp(), that.getIp()));
    }

    public int hashCode() {
        return java.util.Objects.hash(new Object[] { getIp(), Integer.valueOf(getPort()) });
    }

    public String toString() {
        return this.ip + ":" + this.port;
    }

    public static java.util.List<Integer> getIntegerTypes(TYPE... types) {
        java.util.List<Integer> til = new java.util.ArrayList();

        for (TYPE t : types) {
            til.add(mapTypeInteger.get(t));
        }

        return til;
    }

    public HostEntity() {
    }
}
