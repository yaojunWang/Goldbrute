package rdp.gold.brute.rdp;

import org.apache.log4j.Logger;

public abstract class BaseElement {
    public static final int UNLIMITED = -1;
    private final Logger logger = Logger.getLogger(BaseElement.class);
    protected String id;
    protected boolean verbose = false;
    protected int numBuffers = 0;
    protected int packetNumber = 0;
    protected int incommingBufLength = -1;

    public boolean cap(ByteBuffer buf, int minLength, int maxLength, boolean fromCursor) {
        if (buf == null) {
            return false;
        }
        int length = buf.length;
        int cursor;
        if (fromCursor) {
            cursor = buf.cursor;
        } else {
            cursor = 0;
        }
        length -= cursor;
        if (((minLength < 0) || (length >= minLength)) && ((maxLength < 0) || (length <= maxLength))) {
            return true;
        }
        if ((minLength >= 0) && (length < minLength)) {
            if (this.verbose) {
                this.logger.info("Buffer is too small. Min length: " + minLength + ", data length (after cursor): " + length + ".");
            }
            return false;
        }
        if ((maxLength >= 0) && (length > maxLength)) {
            if (this.verbose) {
                this.logger.info("Buffer is too big. Max length: " + maxLength + ", data length (after cursor): " + length + ".");
            }
            buf.length = (maxLength + cursor);
        }
        return true;
    }

    public abstract ByteBuffer proccessPacket(ByteBuffer paramByteBuffer);
}
