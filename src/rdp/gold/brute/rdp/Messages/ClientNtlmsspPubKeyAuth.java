package rdp.gold.brute.rdp.Messages;

import java.nio.charset.Charset;

import rdp.gold.brute.rdp.BaseElement;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.RdpConstants;
import rdp.gold.brute.rdp.Messages.asn1.NegoItem;
import rdp.gold.brute.rdp.Messages.asn1.SubjectPublicKeyInfo;
import rdp.gold.brute.rdp.Messages.asn1.TSRequest;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;
import rdp.gold.brute.rdp.ssl.SSLState;

public class ClientNtlmsspPubKeyAuth extends BaseElement implements NtlmConstants {
    protected NtlmState ntlmState;
    protected SSLState sslState;
    protected String targetDomain;
    protected String user;
    protected String password;
    protected String workstation;
    protected String serverHostName;

    public ClientNtlmsspPubKeyAuth(NtlmState ntlmState, SSLState sslState, String serverHostName, String targetDomain, String workstation, String user, String password) {
        this.ntlmState = ntlmState;
        this.sslState = sslState;
        this.serverHostName = serverHostName;
        this.targetDomain = targetDomain;
        this.workstation = workstation;
        this.user = user;
        this.password = password;
    }

    public static ByteBuffer generateAuthenticateMessage(NtlmState ntlmState) {
        int blocksCursor = 88;

        ByteBuffer buf = new ByteBuffer(4096);

        buf.writeString("NTLMSSP", RdpConstants.CHARSET_8);
        buf.writeByte(0);

        buf.writeIntLE(3);

        blocksCursor = writeBlock(buf, ntlmState.lmChallengeResponse, blocksCursor);

        blocksCursor = writeBlock(buf, ntlmState.ntChallengeResponse, blocksCursor);

        blocksCursor = writeStringBlock(buf, ntlmState.domain, blocksCursor, RdpConstants.CHARSET_16);

        blocksCursor = writeStringBlock(buf, ntlmState.user, blocksCursor, RdpConstants.CHARSET_16);

        blocksCursor = writeStringBlock(buf, ntlmState.workstation, blocksCursor, RdpConstants.CHARSET_16);

        blocksCursor = writeBlock(buf, ntlmState.encryptedRandomSessionKey, blocksCursor);

        buf.writeIntLE(-494357963);

        buf.writeBytes(generateVersion());

        int savedCursorForMIC = buf.cursor;

        buf.writeBytes(new byte[16]);

        if (88 != buf.cursor) {
            throw new RuntimeException("BUG: Actual offset of first byte of allocated blocks is not equal hardcoded offset. Hardcoded offset: 88, actual offset: " + buf.cursor
                    + ". Update hardcoded offset to match actual offset.");
        }

        buf.cursor = blocksCursor;
        buf.trimAtCursor();

        ntlmState.authenticateMessage = buf.toByteArray();

        ntlmState.ntlm_compute_message_integrity_check();
        buf.cursor = savedCursorForMIC;
        buf.writeBytes(ntlmState.messageIntegrityCheck);
        buf.rewindCursor();

        return buf;
    }

    private static int writeStringBlock(ByteBuffer buf, String string, int blocksCursor, Charset charset) {
        return writeBlock(buf, string.getBytes(charset), blocksCursor);
    }

    private static int writeBlock(ByteBuffer buf, byte[] block, int blocksCursor) {
        buf.writeShortLE(block.length);

        buf.writeShortLE(block.length);

        buf.writeIntLE(blocksCursor);

        int savedCursor = buf.cursor;
        buf.cursor = blocksCursor;
        buf.writeBytes(block);
        blocksCursor = buf.cursor;
        buf.cursor = savedCursor;

        return blocksCursor;
    }

    private static byte[] generateVersion() {
        return new byte[] { 6, 1, -79, 29, 0, 0, 0, 15 };
    }

    public ByteBuffer proccessPacket(ByteBuffer byteBuffer) {
        this.ntlmState.domain = this.targetDomain;
        this.ntlmState.user = this.user;
        this.ntlmState.password = this.password;
        this.ntlmState.workstation = this.workstation;
        this.ntlmState.generateServicePrincipalName(this.serverHostName);
        this.ntlmState.ntlm_construct_authenticate_target_info();
        this.ntlmState.ntlm_generate_timestamp();
        this.ntlmState.ntlm_generate_client_challenge();
        this.ntlmState.ntlm_compute_lm_v2_response();
        this.ntlmState.ntlm_compute_ntlm_v2_response();
        this.ntlmState.ntlm_generate_key_exchange_key();
        this.ntlmState.ntlm_generate_random_session_key();
        this.ntlmState.ntlm_generate_exported_session_key();
        this.ntlmState.ntlm_encrypt_random_session_key();
        this.ntlmState.ntlm_init_rc4_seal_states();

        ByteBuffer authenticateMessage = generateAuthenticateMessage(this.ntlmState);
        ByteBuffer messageSignatureAndEncryptedServerPublicKey = generateMessageSignatureAndEncryptedServerPublicKey(this.ntlmState);

        ByteBuffer buf = new ByteBuffer(4096, true);

        TSRequest tsRequest = new TSRequest("TSRequest");
        tsRequest.version.value = Long.valueOf(2L);
        NegoItem negoItem = new NegoItem("NegoItem");
        negoItem.negoToken.value = authenticateMessage;

        tsRequest.negoTokens.tags = new Tag[] { negoItem };

        tsRequest.pubKeyAuth.value = messageSignatureAndEncryptedServerPublicKey;

        tsRequest.writeTag(buf);

        buf.trimAtCursor();

        return buf;
    }

    private byte[] getServerPublicKey() {
        ByteBuffer subjectPublicKeyInfo = new ByteBuffer(this.sslState.serverCertificateSubjectPublicKeyInfo);

        SubjectPublicKeyInfo parser = new SubjectPublicKeyInfo("SubjectPublicKeyInfo");
        parser.readTag(subjectPublicKeyInfo);

        ByteBuffer subjectPublicKey = new ByteBuffer(subjectPublicKeyInfo.length);
        parser.subjectPublicKey.writeTag(subjectPublicKey);

        subjectPublicKeyInfo.unref();
        subjectPublicKey.trimAtCursor();

        subjectPublicKey.trimHeader(5);

        this.ntlmState.subjectPublicKey = subjectPublicKey.toByteArray();

        return this.ntlmState.subjectPublicKey;
    }

    private ByteBuffer generateMessageSignatureAndEncryptedServerPublicKey(NtlmState ntlmState) {
        return new ByteBuffer(ntlmState.ntlm_EncryptMessage(getServerPublicKey()));
    }
}
