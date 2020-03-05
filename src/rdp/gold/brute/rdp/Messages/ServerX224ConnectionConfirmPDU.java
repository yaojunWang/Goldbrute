package rdp.gold.brute.rdp.Messages;

import org.apache.log4j.Logger;
import rdp.gold.brute.rdp.BaseElement;
import rdp.gold.brute.rdp.ByteBuffer;

public class ServerX224ConnectionConfirmPDU extends BaseElement {
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
    public static final int SSL_REQUIRED_BY_SERVER = 1;
    public static final int SSL_NOT_ALLOWED_BY_SERVER = 2;
    public static final int SSL_CERT_NOT_ON_SERVER = 3;
    public static final int INCONSISTENT_FLAGS = 4;
    public static final int HYBRID_REQUIRED_BY_SERVER = 5;
    public static final int SSL_WITH_USER_AUTH_REQUIRED_BY_SERVER = 6;
    private final Logger logger = Logger.getLogger(ServerX224ConnectionConfirmPDU.class);

    public ByteBuffer proccessPacket(ByteBuffer buf) {
        if (buf == null) {
            throw new RuntimeException("Buffer is null");
        }
        if (this.verbose) {
            this.logger.info("Data received: " + buf + ".");
        }
        int x224Length = buf.readVariableSignedIntLE();

        int x224Type = buf.readUnsignedByte();
        if (x224Type != 208) {
            throw new RuntimeException("Unexpected type of packet. Expected type: 208 (CONNECTION CONFIRM), actual type: " + x224Type + ", length: " + x224Length + ", buf: " + buf + ".");
        }

        buf.skipBytes(2);

        buf.skipBytes(2);

        buf.skipBytes(1);

        int negType = buf.readUnsignedByte();

        buf.skipBytes(1);

        int length = buf.readUnsignedShortLE();

        if (length != 8) {
            throw new RuntimeException("Unexpected length of buffer. Expected value: 8, actual value: " + length + ", RDP NEG buf: " + buf + ".");
        }

        int protocol = buf.readSignedIntLE();

        if (negType != 2) {

            int errorCode = protocol;
            String message = "Unknown error.";
            switch (errorCode) {
            case 1:
                message = "The server requires that the client support Enhanced RDP Security with either TLS 1.0, 1.1 or 1.2 or CredSSP. If only CredSSP was requested then the server only supports TLS.";
                break;

            case 2:
                message = "The server is configured to only use Standard RDP Security mechanisms and does not support any External Security Protocols.";
                break;

            case 3:
                message = "The server does not possess a valid authentication certificate and cannot initialize the External Security Protocol Provider.";
                break;

            case 4:
                message = "The list of requested security protocols is not consistent with the current security protocol in effect. This error is only possible when the Direct Approach is used and an External Security Protocolis already being used.";
                break;

            case 5:
                message = "The server requires that the client support Enhanced RDP Security with CredSSP.";
                break;

            case 6:
                message = "The server requires that the client support Enhanced RDP Security  with TLS 1.0, 1.1 or 1.2 and certificate-based client authentication.";
            }

            throw new RuntimeException("Connection failure: " + message);
        }

        if ((protocol != 1) && (protocol != 2)) {
            throw new RuntimeException("Unexpected protocol type (nor SSL, nor HYBRID (SSL+CredSSP)): " + protocol + ", RDP NEG buf: " + buf + ".");
        }
        if (this.verbose) {
            this.logger.info("RDP Negotiation response. Type: " + negType + ", protocol: " + protocol + ".");
        }

        return buf;
    }
}
