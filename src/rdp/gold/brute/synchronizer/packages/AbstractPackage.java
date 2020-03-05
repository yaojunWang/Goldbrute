package rdp.gold.brute.synchronizer.packages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import rdp.gold.brute.synchronizer.SynchronizerInfo;
import rdp.gold.brute.utils.EncryptBytes;

public abstract class AbstractPackage {
    private final Logger logger = Logger.getLogger(getClass());
    private final String packetName;
    private SynchronizerInfo synchronizerInfo;

    public AbstractPackage(SynchronizerInfo synchronizerInfoObject, String packetNameString) {
        this.synchronizerInfo = synchronizerInfoObject;
        this.packetName = packetNameString;
    }

    protected ByteBuffer encrypt(byte[] packet) throws Exception {
        byte[] encryptPacket = EncryptBytes.encryptBytes(packet);
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptPacket);

        return byteBuffer;
    }

    protected ByteBuffer decrypt(byte[] packet) throws Exception {
        byte[] decryptPacket = EncryptBytes.decryptBytes(packet);
        ByteBuffer byteBuffer = ByteBuffer.wrap(decryptPacket);

        return byteBuffer;
    }

    protected ByteBuffer createPacket(byte[] packet) throws Exception {
        return createPacket(packet, true);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected ByteBuffer createPacket(byte[] packet, boolean isEncrypt) throws Exception {
        Map<String, Object> packetMap = new HashMap();

        packetMap.put("PACKET_NAME", this.packetName);
        packetMap.put("PACKET", packet);
        packetMap.put("PACKET_LENGTH", Integer.valueOf(packet.length));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(packetMap);
        objectOutputStream.flush();

        this.logger.info("Create packet: " + packetMap.get("PACKET_NAME"));

        return isEncrypt ? encrypt(byteArrayOutputStream.toByteArray()) : ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Map<String, Object> parsePacket(byte[] packet) throws Exception {
        ByteBuffer decryptPacket = decrypt(packet);

        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(decryptPacket.array()));
        Map<String, Object> packetMap = (Map) objectInputStream.readObject();

        if (!packetMap.get("PACKET_NAME").equals(this.packetName)) {
            throw new Exception("Receive packet name is not equals " + getClass().getSimpleName());
        }

        return packetMap;
    }

    public SynchronizerInfo getSynchronizerInfo() {
        return this.synchronizerInfo;
    }
}
