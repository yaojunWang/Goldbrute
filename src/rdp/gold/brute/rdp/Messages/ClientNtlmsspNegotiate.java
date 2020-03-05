package rdp.gold.brute.rdp.Messages;

import rdp.gold.brute.rdp.BaseElement;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.Messages.asn1.NegoItem;
import rdp.gold.brute.rdp.Messages.asn1.TSRequest;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;
import rdp.gold.brute.rdp.RdpConstants;

public class ClientNtlmsspNegotiate extends BaseElement {
    public NegoFlags clientConfigFlags = new NegoFlags().set_NEGOTIATE_56().set_NEGOTIATE_KEY_EXCH().set_NEGOTIATE_128().set_NEGOTIATE_VERSION().set_NEGOTIATE_EXTENDED_SESSION_SECURITY()
            .set_NEGOTIATE_ALWAYS_SIGN().set_NEGOTIATE_NTLM().set_NEGOTIATE_LM_KEY().set_NEGOTIATE_SEAL().set_NEGOTIATE_SIGN().set_REQUEST_TARGET().set_NEGOTIATE_OEM().set_NEGOTIATE_UNICODE();
    protected NtlmState ntlmState;

    public ClientNtlmsspNegotiate(NtlmState state) {
        this.ntlmState = state;
    }

    public ByteBuffer proccessPacket(ByteBuffer byteBuffer) {
        ByteBuffer negoToken = generateNegotiateMessage();
        this.ntlmState.negotiateMessage = negoToken.toByteArray();

        ByteBuffer buf = new ByteBuffer(1024, true);

        TSRequest tsRequest = new TSRequest("TSRequest");
        tsRequest.version.value = Long.valueOf(2L);
        NegoItem negoItem = new NegoItem("NegoItem");
        negoItem.negoToken.value = negoToken;
        tsRequest.negoTokens.tags = new Tag[] { negoItem };

        tsRequest.writeTag(buf);

        buf.trimAtCursor();

        return buf;
    }

    private ByteBuffer generateNegotiateMessage() {
        ByteBuffer buf = new ByteBuffer(1024);

        buf.writeString("NTLMSSP", RdpConstants.CHARSET_8);
        buf.writeByte(0);

        buf.writeIntLE(1);

        buf.writeIntLE(this.clientConfigFlags.value);

        buf.writeShortLE(0);
        buf.writeShortLE(0);
        buf.writeIntLE(0);

        buf.writeShortLE(0);
        buf.writeShortLE(0);
        buf.writeIntLE(0);

        buf.writeBytes(new byte[] { 6, 1, -79, 29, 0, 0, 0, 15 });

        buf.trimAtCursor();

        return buf;
    }
}
