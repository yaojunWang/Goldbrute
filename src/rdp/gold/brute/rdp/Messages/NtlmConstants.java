package rdp.gold.brute.rdp.Messages;

public abstract interface NtlmConstants {
    public static final int MSV_AV_EOL = 0;
    public static final int MSV_AV_NETBIOS_COMPUTER_NAME = 1;
    public static final int MSV_AV_NETBIOS_DOMAIN_NAME = 2;
    public static final int MSV_AV_DNS_COMPUTER_NAME = 3;
    public static final int MSV_AV_DNS_DOMAIN_NAME = 4;
    public static final int MSV_AV_DNS_TREE_NAME = 5;
    public static final int MSV_AV_FLAGS = 6;
    public static final int MSV_AV_FLAGS_MESSAGE_INTEGRITY_CHECK = 2;
    public static final int MSV_AV_TIMESTAMP = 7;
    public static final int MSV_AV_SINGLE_HOST = 8;
    public static final int MSV_AV_TARGET_NAME = 9;
    public static final int MSV_AV_CHANNEL_BINDINGS = 10;
    public static final String NTLMSSP = "NTLMSSP";
    public static final String GSS_RDP_SERVICE_NAME = "TERMSRV";
    public static final int NEGOTIATE = 1;
    public static final int CHALLENGE = 2;
    public static final int NTLMSSP_AUTH = 3;
    public static final String OID_SPNEGO = "1.3.6.1.5.5.2";
    public static final String OID_KERBEROS5 = "1.2.840.113554.1.2.2";
    public static final String OID_MSKERBEROS5 = "1.2.840.48018.1.2.2";
    public static final String OID_KRB5USERTOUSER = "1.2.840.113554.1.2.2.3";
    public static final String OID_NTLMSSP = "1.3.6.1.4.1.311.2.2.10";
    public static final String LM_MAGIC = "KGS!@#$%";
    public static final String CLIENT_SIGN_MAGIC = "session key to client-to-server signing key magic constant";
    public static final String CLIENT_SEAL_MAGIC = "session key to client-to-server sealing key magic constant";
    public static final String SERVER_SIGN_MAGIC = "session key to server-to-client signing key magic constant";
    public static final String SERVER_SEAL_MAGIC = "session key to server-to-client sealing key magic constant";
    public static final int CHALLENGE_MAX_LIFETIME = 129600;
}
