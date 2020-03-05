package rdp.gold.brute.rdp.Messages;

import org.apache.log4j.Logger;
import rdp.gold.brute.rdp.ByteBuffer;

public class ClientTpkt extends rdp.gold.brute.rdp.BaseElement {
    private final Logger logger = Logger.getLogger(ClientTpkt.class);

    public ByteBuffer proccessPacket(ByteBuffer buf) {
        if (buf == null) {
            throw new RuntimeException("Buffer is null");
        }
        if (this.verbose) {
            this.logger.info("Data received: " + buf + ".");
        }
        if (buf.length + 4 > 65535) {
            throw new RuntimeException("Packet is too long for TPKT (max length 65535-4): " + buf + ".");
        }
        ByteBuffer data = new ByteBuffer(4);

        data.writeByte(3);

        data.writeByte(0);

        data.writeShort(buf.length + 4);

        buf.prepend(data);
        data.unref();

        return buf;
    }
}
