package rdp.gold.brute.rdp.Messages;

import java.util.Arrays;
import javax.crypto.Cipher;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.RdpConstants;

public class NtlmState implements NtlmConstants {
    public NegoFlags negotiatedFlags;
    public byte[] serverTargetInfo;
    public byte[] serverChallenge;
    public byte[] clientChallenge;
    public byte[] keyExchangeKey;
    public byte[] exportedSessionKey;
    public byte[] clientSigningKey;
    public byte[] clientSealingKey;
    public byte[] encryptedRandomSessionKey;
    public byte[] sessionBaseKey;
    public byte[] responseKeyNT;
    public byte[] ntProofStr1;
    public String domain;
    public String user;
    public String workstation;
    public String password;
    public String serverNetbiosDomainName;
    public String serverNetbiosComputerName;
    public String serverDnsDomainName;
    public String serverDnsComputerName;
    public String serverDnsTreeName;
    public String serverTargetName;
    public byte[] serverTimestamp;
    public byte[] clientChallengeTimestamp;
    public byte[] lmChallengeResponse;
    public byte[] ntChallengeResponse;
    public byte[] ntProofStr2;
    public byte[] randomSessionKey;
    public byte[] serverSigningKey;
    public byte[] serverSealingKey;
    public byte[] sendSigningKey;
    public byte[] recvSigningKey;
    public byte[] sendSealingKey;
    public byte[] recvSealingKey;
    public Cipher sendRc4Seal;
    public Cipher recvRc4Seal;
    public byte[] messageIntegrityCheck;
    public byte[] negotiateMessage;
    public byte[] challengeMessage;
    public byte[] authenticateMessage;
    public int sendSeqNum;
    public int recvSeqNum;
    public byte[] authenticateTargetInfo;
    public String servicePrincipalName;
    public byte[] subjectPublicKey;
    private byte[] channelBindingsHash = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public static void main(String[] args) {
        CryptoAlgos.callAll(new NtlmState());
    }

    public byte[] ntlm_generate_timestamp() {
        this.clientChallengeTimestamp = this.serverTimestamp;
        return this.clientChallengeTimestamp;
    }

    public byte[] NTOWFv1W(String password) {
        return CryptoAlgos.MD4(password.getBytes(RdpConstants.CHARSET_16));
    }

    public void testNTOWFv1W() {
        byte[] expected = { 37, -13, 57, -55, -122, -75, -62, 111, -36, -85, -111, 52, -109, -94, 24, 42 };

        byte[] actual = NTOWFv1W("R2Preview!");
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] NTOWFv2W(String password, String user, String domain) {
        return CryptoAlgos.HMAC_MD5(NTOWFv1W(password), (user.toUpperCase() + domain).getBytes(RdpConstants.CHARSET_16));
    }

    public void testNTOWFv2W() {
        byte[] expected = { 95, -52, 76, 72, 116, 107, -108, -50, -73, -82, -15, 13, -55, 17, 34, -113 };

        byte[] actual = NTOWFv2W("R2Preview!", "Administrator", "workgroup");
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_compute_ntlm_v2_hash() {
        return NTOWFv2W(this.password, this.user, this.domain);
    }

    public byte[] ntlm_generate_client_challenge() {
        if (this.clientChallenge == null) {
            this.clientChallenge = CryptoAlgos.NONCE(8);
        }
        return this.clientChallenge;
    }

    public byte[] ntlm_compute_lm_v2_response() {
        if (this.lmChallengeResponse == null) {
            byte[] ntlm_v2_hash = ntlm_compute_ntlm_v2_hash();

            ntlm_generate_client_challenge();

            byte[] challenges = CryptoAlgos.concatenationOf(new byte[][] { this.serverChallenge, this.clientChallenge });

            this.lmChallengeResponse = CryptoAlgos.concatenationOf(new byte[][] { CryptoAlgos.HMAC_MD5(ntlm_v2_hash, challenges), this.clientChallenge });
        }

        return this.lmChallengeResponse;
    }

    public void testComputeLmV2Response() {
        this.serverChallenge = new byte[] { 52, -28, 76, -43, 117, -29, 67, 15 };
        this.clientChallenge = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        this.password = "R2Preview!";
        this.user = "Administrator";
        this.domain = "workgroup";
        byte[] expected = { -88, -82, -41, 70, 6, 50, 2, 53, 29, -107, -103, 54, 32, 54, -84, -61, 1, 2, 3, 4, 5, 6, 7, 8 };

        byte[] actual = ntlm_compute_lm_v2_response();
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] computeNtProofStr(byte[] ntlmV2Hash, byte[] data) {
        return CryptoAlgos.HMAC_MD5(ntlmV2Hash, data);
    }

    public void testComputeNtProofStr() {
        byte[] ntlm_v2_hash = { 95, -52, 76, 72, 116, 107, -108, -50, -73, -82, -15, 13, -55, 17, 34, -113 };

        byte[] data = { 74, 37, 80, -91, 17, -101, -42, 22, 1, 1, 0, 0, 0, 0, 0, 0, -96, -24, -123, 44, -28, -55, -50, 1, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 2, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0,
                76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 1, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82,
                0, 48, 0, 4, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 3, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0,
                52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 7, 0, 8, 0, -96, -24, -123, 44, -28, -55, -50, 1, 6, 0, 4, 0, 2, 0, 0, 0, 10, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 9, 0, 38, 0, 84, 0, 69, 0, 82, 0, 77, 0, 83, 0, 82, 0, 86, 0, 47, 0, 49, 0, 57, 0, 50, 0, 46, 0, 49, 0, 54, 0, 56, 0, 46, 0, 49, 0, 46, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0 };

        byte[] expected = { 25, 75, -21, -83, -38, 36, -43, -106, -123, 46, 36, -108, -42, 74, -72, 94 };

        byte[] actual = computeNtProofStr(ntlm_v2_hash, data);
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] computeSessionBaseKey(byte[] ntlmV2Hash, byte[] ntProofStr) {
        return CryptoAlgos.HMAC_MD5(ntlmV2Hash, ntProofStr);
    }

    public void testComputeSessionBaseKey() {
        byte[] ntlm_v2_hash = { 95, -52, 76, 72, 116, 107, -108, -50, -73, -82, -15, 13, -55, 17, 34, -113 };

        byte[] nt_proof_str = { 25, 75, -21, -83, -38, 36, -43, -106, -123, 46, 36, -108, -42, 74, -72, 94 };

        byte[] expected = { -114, 15, -35, 18, 76, 59, 17, Byte.MAX_VALUE, 34, -71, 75, 89, 82, -68, -89, 24 };

        byte[] actual = computeSessionBaseKey(ntlm_v2_hash, nt_proof_str);
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public String generateServicePrincipalName(String serverHostName) {
        this.servicePrincipalName = ("TERMSRV/" + serverHostName);
        return this.servicePrincipalName;
    }

    public void writeAVPair(ByteBuffer buf, int avPairType, byte[] value) {
        if (value != null) {
            buf.writeShortLE(avPairType);
            buf.writeShortLE(value.length);
            buf.writeBytes(value);
        }
    }

    public void writeAVPair(ByteBuffer buf, int avPairType, String value) {
        if (value != null) {
            writeAVPair(buf, avPairType, value.getBytes(RdpConstants.CHARSET_16));
        }
    }

    public byte[] ntlm_construct_authenticate_target_info() {
        ByteBuffer buf = new ByteBuffer(4096);

        writeAVPair(buf, 2, this.serverNetbiosDomainName);

        writeAVPair(buf, 1, this.serverNetbiosComputerName);

        writeAVPair(buf, 4, this.serverDnsDomainName);

        writeAVPair(buf, 3, this.serverDnsComputerName);

        writeAVPair(buf, 5, this.serverDnsTreeName);

        writeAVPair(buf, 7, this.serverTimestamp);

        byte[] flags = { 2, 0, 0, 0 };
        writeAVPair(buf, 6, flags);

        writeAVPair(buf, 10, this.channelBindingsHash);

        writeAVPair(buf, 9, this.servicePrincipalName);

        writeAVPair(buf, 0, "");

        buf.trimAtCursor();

        this.authenticateTargetInfo = buf.toByteArray();
        buf.unref();

        return this.authenticateTargetInfo;
    }

    public void testConstructAuthenticateTargetInfo() {
        this.serverNetbiosDomainName = "WIN-LO419B2LSR0";
        this.serverNetbiosComputerName = "WIN-LO419B2LSR0";
        this.serverDnsDomainName = "WIN-LO419B2LSR0";
        this.serverDnsComputerName = "WIN-LO419B2LSR0";
        this.serverTimestamp = new byte[] { -96, -24, -123, 44, -28, -55, -50, 1 };
        this.servicePrincipalName = "TERMSRV/192.168.1.3";

        byte[] expected = { 2, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 1, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0,
                52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 4, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 3,
                0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 7, 0, 8, 0, -96, -24, -123, 44, -28, -55, -50, 1, 6, 0, 4, 0, 2, 0,
                0, 0, 10, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 38, 0, 84, 0, 69, 0, 82, 0, 77, 0, 83, 0, 82, 0, 86, 0, 47, 0, 49, 0, 57, 0, 50, 0, 46, 0, 49, 0, 54, 0, 56,
                0, 46, 0, 49, 0, 46, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        byte[] actual = ntlm_construct_authenticate_target_info();
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_compute_ntlm_v2_response() {
        ByteBuffer buf = new ByteBuffer(4096);

        byte[] ntlm_v2_hash = ntlm_compute_ntlm_v2_hash();

        buf.writeByte(1);
        buf.writeByte(1);
        buf.writeShort(0);
        buf.writeInt(0);
        buf.writeBytes(this.clientChallengeTimestamp);
        buf.writeBytes(this.clientChallenge);
        buf.writeInt(0);
        buf.writeBytes(this.authenticateTargetInfo);
        buf.trimAtCursor();
        byte[] bufBytes = buf.toByteArray();
        buf.unref();

        this.ntProofStr2 = computeNtProofStr(ntlm_v2_hash, CryptoAlgos.concatenationOf(new byte[][] { this.serverChallenge, bufBytes }));

        this.ntChallengeResponse = CryptoAlgos.concatenationOf(new byte[][] { this.ntProofStr2, bufBytes });

        this.sessionBaseKey = computeSessionBaseKey(ntlm_v2_hash, this.ntProofStr2);

        return this.ntChallengeResponse;
    }

    public void testComputeNtlmV2Response() {
        this.serverChallenge = new byte[] { 74, 37, 80, -91, 17, -101, -42, 22 };
        this.clientChallenge = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        this.password = "R2Preview!";
        this.user = "Administrator";
        this.domain = "workgroup";
        this.clientChallengeTimestamp = new byte[] { -96, -24, -123, 44, -28, -55, -50, 1 };
        this.authenticateTargetInfo = new byte[] { 2, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 1, 0, 30, 0, 87, 0, 73, 0, 78,
                0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 4, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0,
                83, 0, 82, 0, 48, 0, 3, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 7, 0, 8, 0, -96, -24, -123, 44, -28, -55,
                -50, 1, 6, 0, 4, 0, 2, 0, 0, 0, 10, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 38, 0, 84, 0, 69, 0, 82, 0, 77, 0, 83, 0, 82, 0, 86, 0, 47, 0, 49, 0, 57, 0, 50, 0,
                46, 0, 49, 0, 54, 0, 56, 0, 46, 0, 49, 0, 46, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        byte[] expected = { 25, 75, -21, -83, -38, 36, -43, -106, -123, 46, 36, -108, -42, 74, -72, 94, 1, 1, 0, 0, 0, 0, 0, 0, -96, -24, -123, 44, -28, -55, -50, 1, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
                0, 2, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 1, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0,
                49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 4, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 3, 0, 30,
                0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 7, 0, 8, 0, -96, -24, -123, 44, -28, -55, -50, 1, 6, 0, 4, 0, 2, 0, 0, 0,
                10, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 38, 0, 84, 0, 69, 0, 82, 0, 77, 0, 83, 0, 82, 0, 86, 0, 47, 0, 49, 0, 57, 0, 50, 0, 46, 0, 49, 0, 54, 0, 56, 0, 46,
                0, 49, 0, 46, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        byte[] actual = ntlm_compute_ntlm_v2_response();
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_generate_key_exchange_key() {
        this.keyExchangeKey = this.sessionBaseKey;
        return this.keyExchangeKey;
    }

    public byte[] ntlm_generate_random_session_key() {
        this.randomSessionKey = CryptoAlgos.NONCE(16);
        return this.randomSessionKey;
    }

    public byte[] ntlm_generate_exported_session_key() {
        this.exportedSessionKey = this.randomSessionKey;
        return this.exportedSessionKey;
    }

    public byte[] ntlm_encrypt_random_session_key() {
        this.encryptedRandomSessionKey = CryptoAlgos.RC4K(this.keyExchangeKey, this.randomSessionKey);
        return this.encryptedRandomSessionKey;
    }

    public void testComputeEncryptedRandomSessionKey() {
        this.keyExchangeKey = new byte[] { -114, 15, -35, 18, 76, 59, 17, Byte.MAX_VALUE, 34, -71, 75, 89, 82, -68, -89, 24 };

        this.randomSessionKey = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        byte[] expected = { -28, -23, -62, -83, 65, 2, 47, 60, -7, 76, 114, -124, -59, 42, 124, 111 };

        byte[] actual = ntlm_encrypt_random_session_key();

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_generate_signing_key(String signMagic) {
        return CryptoAlgos.MD5(CryptoAlgos.concatenationOf(new byte[][] { this.exportedSessionKey, signMagic.getBytes(RdpConstants.CHARSET_8), { 0 } }));
    }

    public void testGenerateSigningKey() {
        this.exportedSessionKey = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        byte[] expected = { -10, -82, -106, -53, 5, -30, -85, 84, -10, -35, 89, -13, -55, -39, -96, 67 };

        byte[] actual = ntlm_generate_signing_key("session key to client-to-server signing key magic constant");

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_generate_client_signing_key() {
        this.clientSigningKey = ntlm_generate_signing_key("session key to client-to-server signing key magic constant");
        return this.clientSigningKey;
    }

    public void testGenerateClientSigningKey() {
        this.exportedSessionKey = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        byte[] expected = { -10, -82, -106, -53, 5, -30, -85, 84, -10, -35, 89, -13, -55, -39, -96, 67 };

        byte[] actual = ntlm_generate_client_signing_key();

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_generate_server_signing_key() {
        this.serverSigningKey = ntlm_generate_signing_key("session key to server-to-client signing key magic constant");
        return this.serverSigningKey;
    }

    public void testGenerateServerSigningKey() {
        this.exportedSessionKey = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        byte[] expected = { -74, 88, -59, -104, 122, 37, -8, 110, -40, -27, 108, -23, 62, 60, -64, -120 };

        byte[] actual = ntlm_generate_server_signing_key();

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_generate_client_sealing_key() {
        this.clientSealingKey = ntlm_generate_signing_key("session key to client-to-server sealing key magic constant");
        return this.clientSealingKey;
    }

    public void testGenerateClientSealingKey() {
        this.exportedSessionKey = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        byte[] expected = { 88, 25, 68, -62, 122, -58, 52, 69, -28, -72, 43, 85, -71, 11, 31, -75 };

        byte[] actual = ntlm_generate_client_sealing_key();

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_generate_server_sealing_key() {
        this.serverSealingKey = ntlm_generate_signing_key("session key to server-to-client sealing key magic constant");
        return this.serverSealingKey;
    }

    public void testGenerateServerSealingKey() {
        this.exportedSessionKey = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        byte[] expected = { -110, 58, 115, 92, -110, -89, 4, 52, -66, -102, -94, -97, -19, -63, -26, 19 };

        byte[] actual = ntlm_generate_server_sealing_key();

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public void ntlm_init_rc4_seal_states() {
        ntlm_generate_client_signing_key();
        ntlm_generate_server_signing_key();
        ntlm_generate_client_sealing_key();
        ntlm_generate_server_sealing_key();

        this.sendSigningKey = this.clientSigningKey;
        this.recvSigningKey = this.serverSigningKey;
        this.sendSealingKey = this.clientSealingKey;
        this.recvSealingKey = this.serverSealingKey;

        this.sendRc4Seal = CryptoAlgos.initRC4(this.sendSealingKey);
        this.recvRc4Seal = CryptoAlgos.initRC4(this.recvSealingKey);
    }

    public byte[] ntlm_compute_message_integrity_check() {
        this.messageIntegrityCheck = CryptoAlgos.HMAC_MD5(this.exportedSessionKey,
                CryptoAlgos.concatenationOf(new byte[][] { this.negotiateMessage, this.challengeMessage, this.authenticateMessage }));

        return this.messageIntegrityCheck;
    }

    public void testComputeMessageIntegrityCheck() {
        this.exportedSessionKey = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        this.negotiateMessage = new byte[] { 78, 84, 76, 77, 83, 83, 80, 0, 1, 0, 0, 0, -73, -126, 8, -30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 1, -79, 29, 0, 0, 0, 15 };

        this.challengeMessage = new byte[] { 78, 84, 76, 77, 83, 83, 80, 0, 2, 0, 0, 0, 30, 0, 30, 0, 56, 0, 0, 0, 53, -126, -118, -30, 74, 37, 80, -91, 17, -101, -42, 22, 0, 0, 0, 0, 0, 0, 0, 0,
                -104, 0, -104, 0, 86, 0, 0, 0, 6, 3, -41, 36, 0, 0, 0, 15, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 2, 0, 30, 0, 87, 0,
                73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 1, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50,
                0, 76, 0, 83, 0, 82, 0, 48, 0, 4, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 3, 0, 30, 0, 87, 0, 73, 0, 78, 0,
                45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 7, 0, 8, 0, -96, -24, -123, 44, -28, -55, -50, 1, 0, 0, 0, 0 };

        this.authenticateMessage = new byte[] { 78, 84, 76, 77, 83, 83, 80, 0, 3, 0, 0, 0, 24, 0, 24, 0, -112, 0, 0, 0, 22, 1, 22, 1, -88, 0, 0, 0, 18, 0, 18, 0, 88, 0, 0, 0, 26, 0, 26, 0, 106, 0, 0,
                0, 12, 0, 12, 0, -124, 0, 0, 0, 16, 0, 16, 0, -66, 1, 0, 0, 53, -78, -120, -30, 6, 1, -79, 29, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 119, 0, 111, 0, 114, 0, 107,
                0, 103, 0, 114, 0, 111, 0, 117, 0, 112, 0, 65, 0, 100, 0, 109, 0, 105, 0, 110, 0, 105, 0, 115, 0, 116, 0, 114, 0, 97, 0, 116, 0, 111, 0, 114, 0, 97, 0, 112, 0, 111, 0, 108, 0, 108, 0,
                111, 0, 124, -64, -3, 8, -59, 20, 5, 52, -13, 18, -98, 62, -93, 9, -68, -58, 1, 2, 3, 4, 5, 6, 7, 8, 25, 75, -21, -83, -38, 36, -43, -106, -123, 46, 36, -108, -42, 74, -72, 94, 1, 1,
                0, 0, 0, 0, 0, 0, -96, -24, -123, 44, -28, -55, -50, 1, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 2, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0,
                76, 0, 83, 0, 82, 0, 48, 0, 1, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 4, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45,
                0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0, 82, 0, 48, 0, 3, 0, 30, 0, 87, 0, 73, 0, 78, 0, 45, 0, 76, 0, 79, 0, 52, 0, 49, 0, 57, 0, 66, 0, 50, 0, 76, 0, 83, 0,
                82, 0, 48, 0, 7, 0, 8, 0, -96, -24, -123, 44, -28, -55, -50, 1, 6, 0, 4, 0, 2, 0, 0, 0, 10, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 38, 0, 84, 0, 69, 0, 82, 0,
                77, 0, 83, 0, 82, 0, 86, 0, 47, 0, 49, 0, 57, 0, 50, 0, 46, 0, 49, 0, 54, 0, 56, 0, 46, 0, 49, 0, 46, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -28, -23, -62, -83, 65,
                2, 47, 60, -7, 76, 114, -124, -59, 42, 124, 111 };

        byte[] expected = { -39, -23, -68, -101, 111, -91, -7, -56, 112, 22, 16, 32, -8, -15, 97, 66 };

        byte[] actual = ntlm_compute_message_integrity_check();

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_EncryptMessage(byte[] message) {
        byte[] versionBytes = { 1, 0, 0, 0 };

        byte[] seqNumBytes = { (byte) (this.sendSeqNum & 0xFF), (byte) (this.sendSeqNum >> 8 & 0xFF), (byte) (this.sendSeqNum >> 16 & 0xFF), (byte) (this.sendSeqNum >> 24 & 0xFF) };

        byte[] digest = CryptoAlgos.HMAC_MD5(this.sendSigningKey, CryptoAlgos.concatenationOf(new byte[][] { seqNumBytes, message }));

        byte[] encrypted = CryptoAlgos.RC4(this.sendRc4Seal, message);

        byte[] checksum = CryptoAlgos.RC4(this.sendRc4Seal, Arrays.copyOf(digest, 8));

        byte[] signature = CryptoAlgos.concatenationOf(new byte[][] { versionBytes, checksum, seqNumBytes });

        this.sendSeqNum += 1;

        return CryptoAlgos.concatenationOf(new byte[][] { signature, encrypted });
    }

    public void testNtlmEncryptMessage() {
        this.sendSigningKey = new byte[] { -10, -82, -106, -53, 5, -30, -85, 84, -10, -35, 89, -13, -55, -39, -96, 67 };

        this.sendRc4Seal = CryptoAlgos.initRC4(new byte[] { 88, 25, 68, -62, 122, -58, 52, 69, -28, -72, 43, 85, -71, 11, 31, -75 });

        this.sendSeqNum = 0;
        byte[] serverPublicKey = { 48, -126, 1, 10, 2, -126, 1, 1, 0, -88, 86, 101, -45, -50, -118, 84, 77, -99, -80, -124, 49, 25, 113, Byte.MAX_VALUE, -35, 66, -5, 42, 122, 114, 19, -95, -71, 114,
                -69, -45, 8, -83, 125, 108, 21, 101, 3, -47, -60, 84, -59, 51, 107, 125, 105, -119, 94, -2, -32, 1, -64, 126, -101, -53, 93, 101, 54, -51, 119, 93, -13, 122, 91, 41, 68, 114, -43, 56,
                -30, -49, -79, -57, 120, -101, 88, -71, 23, 124, -73, -42, -57, -57, -65, -112, 78, 124, 57, -109, -53, 46, -32, -62, 51, 45, -91, 126, -32, 123, -74, -7, -111, 50, -73, -44, -123,
                -73, 53, 45, 43, 0, 109, -8, -22, -116, -105, 95, 81, 29, 104, 4, 60, 121, 20, 113, -89, -57, -41, 112, 122, -32, -70, 18, 105, -56, -45, -39, 78, -85, 81, 71, -93, -20, -103, -44,
                -120, -54, -38, -62, Byte.MAX_VALUE, 121, 75, 102, -19, -121, -66, -62, 95, -22, -49, -31, -75, -16, 61, -101, -14, 25, -61, -32, -31, 122, 69, 113, 18, 61, 114, 29, 111, 43, 28, 70,
                104, -64, -113, 79, -50, 58, -59, -51, 34, 101, 45, 67, -80, 92, -35, -119, -82, -66, 112, 89, 94, 12, -67, -11, 70, -126, 30, -28, -122, -107, 123, 96, -82, 69, 80, -62, 84, 8, 73,
                -102, -98, -5, -78, -74, 120, -27, 47, -100, 90, -48, -118, 3, 119, 104, 48, -109, 120, 109, -112, 109, 80, -6, -89, 101, -2, 89, 51, 39, 78, 75, -8, 56, 68, 58, 18, -12, 7, -96, -115,
                2, 3, 1, 0, 1 };

        byte[] expected = { 1, 0, 0, 0, 114, 118, 30, 87, 73, -75, 15, -83, 0, 0, 0, 0, 21, -9, -14, 84, -38, -87, -27, -83, -123, 4, 103, 77, 11, -53, -7, -79, -8, 2, -118, 119, -62, 99, -85, -43,
                116, 35, -97, -99, 93, 31, -45, -77, -96, -84, 22, -118, 75, 8, -11, 71, 112, 88, 16, -76, -25, -121, -77, 75, -55, -94, -43, -47, -54, 15, -44, -29, -115, 118, 90, 96, 40, -8, 6, 93,
                -28, 126, 33, -56, -69, -84, -27, 121, -123, 48, -101, -120, 19, 47, -113, -4, 4, 82, -2, -121, -108, -49, -53, 73, 74, -38, 111, -35, -18, 87, -91, -28, 77, 14, 92, 61, 11, 99, 31,
                -10, 61, 27, -82, 90, -10, 66, 42, 70, -6, 66, 113, 103, 70, 2, 113, -22, 81, -104, -9, -44, 67, -65, -114, -24, 60, -56, -6, 121, -99, -116, -4, -62, 66, -55, -69, -48, -85, -127,
                -60, 83, -3, 65, -38, -85, 15, 37, 121, 95, -67, -93, -116, -45, -11, 27, -85, 32, -47, -12, -40, -127, -100, 24, 74, -92, 119, -18, -31, 81, -18, 42, -63, -108, 55, -59, 6, 122, 63,
                15, 37, 91, 78, 106, -36, 11, 98, 111, 18, -125, 3, -82, 78, -50, 43, 110, -44, -43, 35, 39, -10, -90, 56, 103, -20, -107, -126, -58, -70, -44, -10, -26, 34, 125, -71, -28, -127, -105,
                36, -1, 64, -78, 66, 60, 17, 36, -48, 58, -106, -39, -63, 19, -42, 98, 69, 33, 96, 91, 123, 43, 98, 68, -9, 64, -109, 41, 91, 68, -73, -38, -100, -90, -87, 59, -31, 59, -99, 49, -14,
                33, 83, 15, -77, 112, 85, -124, 44, -76 };

        byte[] actual = ntlm_EncryptMessage(serverPublicKey);

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public byte[] ntlm_DecryptMessage(byte[] wrappedMssage) {
        byte[] versionBytes = { 1, 0, 0, 0 };

        byte[] seqNumBytes = { (byte) (this.recvSeqNum & 0xFF), (byte) (this.recvSeqNum >> 8 & 0xFF), (byte) (this.recvSeqNum >> 16 & 0xFF), (byte) (this.recvSeqNum >> 24 & 0xFF) };

        byte[] actualSignature = Arrays.copyOf(wrappedMssage, 16);
        byte[] encryptedMessage = Arrays.copyOfRange(wrappedMssage, 16, wrappedMssage.length);

        byte[] decryptedMessage = CryptoAlgos.RC4(this.recvRc4Seal, encryptedMessage);

        byte[] digest = CryptoAlgos.HMAC_MD5(this.recvSigningKey, CryptoAlgos.concatenationOf(new byte[][] { seqNumBytes, decryptedMessage }));

        byte[] checksum = CryptoAlgos.RC4(this.recvRc4Seal, Arrays.copyOf(digest, 8));

        byte[] expectedSignature = CryptoAlgos.concatenationOf(new byte[][] { versionBytes, checksum, seqNumBytes });

        if (!Arrays.equals(expectedSignature, actualSignature)) {
            throw new RuntimeException("Unexpected signature of message:\nExpected signature: " + new ByteBuffer(expectedSignature).toPlainHexString() + "\n  Actual signature: "
                    + new ByteBuffer(actualSignature).toPlainHexString());
        }
        this.recvSeqNum += 1;

        return decryptedMessage;
    }

    public void testNtlmDecryptMessage() {
        this.recvSigningKey = new byte[] { -74, 88, -59, -104, 122, 37, -8, 110, -40, -27, 108, -23, 62, 60, -64, -120 };

        this.recvRc4Seal = CryptoAlgos.initRC4(new byte[] { -110, 58, 115, 92, -110, -89, 4, 52, -66, -102, -94, -97, -19, -63, -26, 19 });

        this.recvSeqNum = 0;
        byte[] encryptedMessage = { 1, 0, 0, 0, 37, -8, 45, 30, 78, 106, -20, 79, 0, 0, 0, 0, 3, 18, -35, -22, 71, -77, -1, -31, 102, 8, -10, 107, -96, 98, 66, 103, -65, 61, 89, 96, -17, 82, -80, 38,
                -107, -19, -124, 72, 68, -69, -115, 101, -49, -28, -114, 111, 105, -82, -19, 68, -69, 73, 29, 42, 64, 41, 43, 19, 66, 28, -21, -79, 108, -118, 59, Byte.MIN_VALUE, -47, 112, -3, -35,
                121, -28, -109, 11, 71, -67, 58, 126, 49, 102, 75, 101, -115, 92, 42, -51, -62, 9, 122, 59, -78, -3, 9, 82, 9, 71, 5, -92, 111, 50, -47, 118, -78, -44, 89, -32, -123, -15, 54, 125,
                118, 80, 33, 14, 32, 34, -125, 26, 8, -64, -123, 93, 79, 92, 119, 104, 50, -107, -87, -94, 89, 105, -22, 25, 52, 8, -19, 118, -93, 88, 55, -14, 10, 12, -70, 77, -69, 111, -126, -108,
                -45, -121, -34, -55, -113, -17, 52, 45, -113, -48, 12, -111, 89, -3, -22, 107, -53, -67, -94, 32, -19, -71, 118, -45, 100, 27, -77, 59, -11, -101, 97, -41, -85, 38, -101, 13, -96, -22,
                -65, -83, 44, -83, 99, 101, -58, 112, -60, -27, -115, 64, -86, 8, 69, 102, -30, 77, -55, 70, 0, 51, 67, -32, -70, -42, Byte.MIN_VALUE, 41, 33, 94, -47, -102, -68, 68, -6, 77, 70, -7,
                37, Byte.MIN_VALUE, 64, -75, 39, -35, -59, 2, -8, -92, -102, -53, -49, 63, -17, -57, -51, 113, 69, -91, 53, -79, 33, 20, 57, 87, -8, 10, 36, -104, -22, 21, -31, -29, -53, -99, -14, 78,
                -17, -119, -105, -64, -78, -106, -102, 30, -83, -48, -102, -103, 98, -97, 19, 46 };

        byte[] expected = { 49, -126, 1, 10, 2, -126, 1, 1, 0, -88, 86, 101, -45, -50, -118, 84, 77, -99, -80, -124, 49, 25, 113, Byte.MAX_VALUE, -35, 66, -5, 42, 122, 114, 19, -95, -71, 114, -69,
                -45, 8, -83, 125, 108, 21, 101, 3, -47, -60, 84, -59, 51, 107, 125, 105, -119, 94, -2, -32, 1, -64, 126, -101, -53, 93, 101, 54, -51, 119, 93, -13, 122, 91, 41, 68, 114, -43, 56, -30,
                -49, -79, -57, 120, -101, 88, -71, 23, 124, -73, -42, -57, -57, -65, -112, 78, 124, 57, -109, -53, 46, -32, -62, 51, 45, -91, 126, -32, 123, -74, -7, -111, 50, -73, -44, -123, -73, 53,
                45, 43, 0, 109, -8, -22, -116, -105, 95, 81, 29, 104, 4, 60, 121, 20, 113, -89, -57, -41, 112, 122, -32, -70, 18, 105, -56, -45, -39, 78, -85, 81, 71, -93, -20, -103, -44, -120, -54,
                -38, -62, Byte.MAX_VALUE, 121, 75, 102, -19, -121, -66, -62, 95, -22, -49, -31, -75, -16, 61, -101, -14, 25, -61, -32, -31, 122, 69, 113, 18, 61, 114, 29, 111, 43, 28, 70, 104, -64,
                -113, 79, -50, 58, -59, -51, 34, 101, 45, 67, -80, 92, -35, -119, -82, -66, 112, 89, 94, 12, -67, -11, 70, -126, 30, -28, -122, -107, 123, 96, -82, 69, 80, -62, 84, 8, 73, -102, -98,
                -5, -78, -74, 120, -27, 47, -100, 90, -48, -118, 3, 119, 104, 48, -109, 120, 109, -112, 109, 80, -6, -89, 101, -2, 89, 51, 39, 78, 75, -8, 56, 68, 58, 18, -12, 7, -96, -115, 2, 3, 1,
                0, 1 };

        byte[] actual = ntlm_DecryptMessage(encryptedMessage);

        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("Incorrect result.\nExpected:\n" + new ByteBuffer(expected).toPlainHexString() + "\n  actual:\n" + new ByteBuffer(actual).toPlainHexString() + ".");
        }
    }

    public String toString() {
        return

        "NtlmState{negotiatedFlags=" + this.negotiatedFlags + ", serverTargetInfo=" + Arrays.toString(this.serverTargetInfo) + ", serverChallenge=" + Arrays.toString(this.serverChallenge)
                + ", clientChallenge=" + Arrays.toString(this.clientChallenge) + ", keyExchangeKey=" + Arrays.toString(this.keyExchangeKey) + ", exportedSessionKey="
                + Arrays.toString(this.exportedSessionKey) + ", clientSigningKey=" + Arrays.toString(this.clientSigningKey) + ", clientSealingKey=" + Arrays.toString(this.clientSealingKey)
                + ", encryptedRandomSessionKey=" + Arrays.toString(this.encryptedRandomSessionKey) + ", sessionBaseKey=" + Arrays.toString(this.sessionBaseKey) + ", responseKeyNT="
                + Arrays.toString(this.responseKeyNT) + ", ntProofStr1=" + Arrays.toString(this.ntProofStr1) + ", domain='" + this.domain + '\'' + ", user='" + this.user + '\'' + ", workstation='"
                + this.workstation + '\'' + ", password='" + this.password + '\'' + ", serverNetbiosDomainName='" + this.serverNetbiosDomainName + '\'' + ", serverNetbiosComputerName='"
                + this.serverNetbiosComputerName + '\'' + ", serverDnsDomainName='" + this.serverDnsDomainName + '\'' + ", serverDnsComputerName='" + this.serverDnsComputerName + '\''
                + ", serverDnsTreeName='" + this.serverDnsTreeName + '\'' + ", serverTargetName='" + this.serverTargetName + '\'' + ", serverTimestamp=" + Arrays.toString(this.serverTimestamp)
                + ", clientChallengeTimestamp=" + Arrays.toString(this.clientChallengeTimestamp) + ", lmChallengeResponse=" + Arrays.toString(this.lmChallengeResponse) + ", ntChallengeResponse="
                + Arrays.toString(this.ntChallengeResponse) + ", ntProofStr2=" + Arrays.toString(this.ntProofStr2) + ", randomSessionKey=" + Arrays.toString(this.randomSessionKey)
                + ", serverSigningKey=" + Arrays.toString(this.serverSigningKey) + ", serverSealingKey=" + Arrays.toString(this.serverSealingKey) + ", sendSigningKey="
                + Arrays.toString(this.sendSigningKey) + ", recvSigningKey=" + Arrays.toString(this.recvSigningKey) + ", sendSealingKey=" + Arrays.toString(this.sendSealingKey) + ", recvSealingKey="
                + Arrays.toString(this.recvSealingKey) + ", sendRc4Seal=" + this.sendRc4Seal + ", recvRc4Seal=" + this.recvRc4Seal + ", messageIntegrityCheck="
                + Arrays.toString(this.messageIntegrityCheck) + ", negotiateMessage=" + Arrays.toString(this.negotiateMessage) + ", challengeMessage=" + Arrays.toString(this.challengeMessage)
                + ", authenticateMessage=" + Arrays.toString(this.authenticateMessage) + ", sendSeqNum=" + this.sendSeqNum + ", recvSeqNum=" + this.recvSeqNum + ", authenticateTargetInfo="
                + Arrays.toString(this.authenticateTargetInfo) + ", servicePrincipalName='" + this.servicePrincipalName + '\'' + ", channelBindingsHash=" + Arrays.toString(this.channelBindingsHash)
                + ", subjectPublicKey=" + Arrays.toString(this.subjectPublicKey) + '}';
    }
}
