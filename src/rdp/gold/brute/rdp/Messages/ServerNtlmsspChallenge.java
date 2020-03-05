package rdp.gold.brute.rdp.Messages;

import org.apache.log4j.Logger;

import rdp.gold.brute.rdp.BaseElement;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.ConstantTimeComparator;
import rdp.gold.brute.rdp.RdpConstants;
import rdp.gold.brute.rdp.Messages.asn1.NegoItem;
import rdp.gold.brute.rdp.Messages.asn1.TSRequest;

public class ServerNtlmsspChallenge extends BaseElement implements NtlmConstants {
    private final Logger logger = Logger.getLogger(ServerNtlmsspChallenge.class);
    protected NtlmState ntlmState;

    public ServerNtlmsspChallenge(NtlmState state) {
        this.ntlmState = state;
    }

    public static String readStringByDescription(ByteBuffer buf) {
        ByteBuffer block = readBlockByDescription(buf);
        String value = block.readString(block.length, RdpConstants.CHARSET_16);
        block.unref();

        return value;
    }

    public static ByteBuffer readBlockByDescription(ByteBuffer buf) {
        int blockLength = buf.readUnsignedShortLE();
        int allocatedSpace = buf.readUnsignedShortLE();
        int offset = buf.readSignedIntLE();

        if (allocatedSpace < blockLength) {
            blockLength = allocatedSpace;
        }
        if ((offset > buf.length) || (offset < 0) || (offset + allocatedSpace > buf.length)) {
            throw new RuntimeException("ERROR: NTLM block is too long. Allocated space: " + allocatedSpace + ", block offset: " + offset + ", data: " + buf + ".");
        }

        int storedCursor = buf.cursor;
        buf.cursor = offset;

        ByteBuffer value = buf.readBytes(blockLength);

        buf.cursor = storedCursor;

        return value;
    }

    public ByteBuffer proccessPacket(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            throw new RuntimeException("Buffer is null");
        }
        if (this.verbose) {
            this.logger.info("Data received: " + byteBuffer + ".");
        }

        TSRequest request = new TSRequest("TSRequest");
        request.readTag(byteBuffer);

        ByteBuffer negoToken = ((NegoItem) request.negoTokens.tags[0]).negoToken.value;
        this.ntlmState.challengeMessage = negoToken.toByteArray();

        parseNtlmChallenge(negoToken);

        negoToken.unref();
        byteBuffer.unref();

        return null;
    }

    public void parseNtlmChallenge(ByteBuffer buf) {
        String signature = buf.readVariableString(RdpConstants.CHARSET_8);
        if (!ConstantTimeComparator.compareStrings(signature, "NTLMSSP")) {
            throw new RuntimeException("Unexpected NTLM message singature: \"" + signature + "\". Expected signature: \"" + "NTLMSSP" + "\". Data: " + buf + ".");
        }

        int messageType = buf.readSignedIntLE();
        if (messageType != 2) {
            throw new RuntimeException("Unexpected NTLM message type: " + messageType + ". Expected type: CHALLENGE (" + 2 + "). Data: " + buf + ".");
        }

        this.ntlmState.serverTargetName = readStringByDescription(buf);

        this.ntlmState.negotiatedFlags = new NegoFlags(buf.readSignedIntLE());
        if (this.verbose) {
            this.logger.info("Server negotiate flags: " + this.ntlmState.negotiatedFlags + ".");
        }

        ByteBuffer challenge = buf.readBytes(8);
        this.ntlmState.serverChallenge = challenge.toByteArray();
        if (this.verbose)
            this.logger.info("Server challenge: " + challenge + ".");
        challenge.unref();

        buf.skipBytes(8);

        ByteBuffer targetInfo = readBlockByDescription(buf);

        this.ntlmState.serverTargetInfo = targetInfo.toByteArray();

        parseTargetInfo(targetInfo);
        targetInfo.unref();

        buf.unref();
    }

    public void parseTargetInfo(ByteBuffer buf) {
        while (buf.remainderLength() > 0) {
            int type = buf.readUnsignedShortLE();
            int length = buf.readUnsignedShortLE();

            if (type == 0) {
                break;
            }

            ByteBuffer data = buf.readBytes(length);
            parseAttribute(data, type, length);
            data.unref();
        }
    }

    public void parseAttribute(ByteBuffer buf, int type, int length) {
        switch (type) {
        case 2:
            this.ntlmState.serverNetbiosDomainName = buf.readString(length, RdpConstants.CHARSET_16);
            break;
        case 1:
            this.ntlmState.serverNetbiosComputerName = buf.readString(length, RdpConstants.CHARSET_16);
            break;
        case 4:
            this.ntlmState.serverDnsDomainName = buf.readString(length, RdpConstants.CHARSET_16);
            break;
        case 3:
            this.ntlmState.serverDnsComputerName = buf.readString(length, RdpConstants.CHARSET_16);
            break;
        case 5:
            this.ntlmState.serverDnsTreeName = buf.readString(length, RdpConstants.CHARSET_16);
            break;

        case 7:
            ByteBuffer tmp = buf.readBytes(length);
            this.ntlmState.serverTimestamp = tmp.toByteArray();

            tmp.unref();
            break;
        }

    }

    public NtlmState getNtlmState() {
        return this.ntlmState;
    }
}
