package rdp.gold.brute.rdp.Messages;

import rdp.gold.brute.rdp.BaseElement;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.RdpConstants;

public class ClientX224ConnectionRequestPDU extends BaseElement {
    public static final int X224_TPDU_CONNECTION_REQUEST = 224;
    public static final int X224_TPDU_CONNECTION_CONFIRM = 208;
    public static final int X224_TPDU_DISCONNECTION_REQUEST = 128;
    public static final int X224_TPDU_DISCONNECTION_CONFIRM = 192;
    public static final int X224_TPDU_EXPEDITED_DATA = 16;
    public static final int X224_TPDU_DATA_ACKNOWLEDGE = 97;
    public static final int X224_TPDU_EXPEDITET_ACKNOWLEDGE = 64;
    public static final int X224_TPDU_REJECT = 81;
    public static final int X224_TPDU_ERROR = 112;
    public static final int X224_TPDU_PROTOCOL_IDENTIFIER = 1;
    protected String userName;
    protected int protocol;

    public ClientX224ConnectionRequestPDU(String userName, int protocol) {
        this.userName = userName;
        this.protocol = protocol;
    }

    public ByteBuffer proccessPacket(ByteBuffer byteBuffer) {
        int length = 33 + this.userName.length();
        ByteBuffer buf = new ByteBuffer(length, true);

        buf.writeByte(224);

        buf.writeShort(0);
        buf.writeShort(0);
        buf.writeByte(0);
        buf.writeString("Cookie: mstshash=" + this.userName + "\r\n", RdpConstants.CHARSET_8);

        buf.writeByte(1);

        buf.writeByte(0);

        buf.writeByte(8);
        buf.writeByte(0);

        buf.writeIntLE(this.protocol);

        ByteBuffer data = new ByteBuffer(5);

        data.writeVariableIntLE(buf.length);

        data.length = data.cursor;

        buf.prepend(data);
        data.unref();

        return buf;
    }
}
