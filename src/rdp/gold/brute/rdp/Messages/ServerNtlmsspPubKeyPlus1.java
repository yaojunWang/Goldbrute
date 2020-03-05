package rdp.gold.brute.rdp.Messages;

import java.util.Arrays;

import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.Messages.asn1.TSRequest;

public class ServerNtlmsspPubKeyPlus1 extends rdp.gold.brute.rdp.BaseElement {
    protected NtlmState ntlmState;

    public ServerNtlmsspPubKeyPlus1(NtlmState ntlmState) {
        this.ntlmState = ntlmState;
    }

    public ByteBuffer proccessPacket(ByteBuffer byteBuffer) {
        TSRequest tsRequest = new TSRequest("TSRequest");
        tsRequest.readTag(byteBuffer);

        ByteBuffer encryptedPubKey = tsRequest.pubKeyAuth.value;
        if ((encryptedPubKey == null) || (encryptedPubKey.length == 0)) {
            throw new RuntimeException("[" + this + "] ERROR: Unexpected message from RDP server. Expected encrypted server public key but got nothing instead. Data: " + byteBuffer);
        }

        byte[] decryptedPubKey = this.ntlmState.ntlm_DecryptMessage(encryptedPubKey.toByteArray());
        int

        tmp86_85 = 0;
        byte[] tmp86_83 = decryptedPubKey;
        tmp86_83[tmp86_85] = ((byte) (tmp86_83[tmp86_85] - 1));

        if (!Arrays.equals(decryptedPubKey, this.ntlmState.subjectPublicKey)) {

            throw new RuntimeException("[" + this + "] ERROR: Unexpected message from RDP server. Expected encrypted server public key but an unknown response. Encryted key after decryption: "
                    + new ByteBuffer(decryptedPubKey).toPlainHexString());
        }
        byteBuffer.unref();

        return null;
    }
}
