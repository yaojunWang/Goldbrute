package rdp.gold.brute.rdp.Messages;

import org.apache.log4j.Logger;
import rdp.gold.brute.rdp.BaseElement;
import rdp.gold.brute.rdp.ByteBuffer;

public class ServerTpkt extends BaseElement {
    public static final int PROTOCOL_TPKT = 3;
    private final Logger logger = Logger.getLogger(ServerTpkt.class);

    public ByteBuffer proccessPacket(ByteBuffer buf) {
        if (buf == null) {
            throw new RuntimeException("Buffer is null");
        }
        if (this.verbose) {
            this.logger.info("Data received: " + buf + ".");
        }

        if (!cap(buf, 4, -1, false)) {
            throw new RuntimeException("Error read 4 bytes");
        }
        int version = buf.readUnsignedByte();
        if (version != 3) {
            throw new RuntimeException("Unexpected data in TPKT header. Expected TPKT version: 0x03,  actual value: " + buf + ".");
        }
        buf.skipBytes(1);

        int length = buf.readUnsignedShort();
        if (!cap(buf, length, length, false)) {
            throw new RuntimeException("Error read unsigned short length");
        }
        int payloadLength = length - buf.cursor;

        ByteBuffer outBuf = buf.slice(buf.cursor, payloadLength, true);
        buf.unref();

        return outBuf;
    }
}
