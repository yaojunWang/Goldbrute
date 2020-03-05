package rdp.gold.brute.rdp.Messages;

import org.apache.log4j.Logger;

public class NegoFlags {
    public static final int NTLMSSP_NEGOTIATE_56 = Integer.MIN_VALUE;
    public static final int NTLMSSP_NEGOTIATE_KEY_EXCH = 1073741824;
    public static final int NTLMSSP_NEGOTIATE_128 = 536870912;
    public static final int NTLMSSP_NEGOTIATE_VERSION = 33554432;
    public static final int NTLMSSP_NEGOTIATE_TARGET_INFO = 8388608;
    public static final int NTLMSSP_REQUEST_NON_NT_SESSION_KEY = 4194304;
    public static final int NTLMSSP_NEGOTIATE_IDENTIFY = 1048576;
    public static final int NTLMSSP_NEGOTIATE_EXTENDED_SESSION_SECURITY = 524288;
    public static final int NTLMSSP_TARGET_TYPE_SERVER = 131072;
    public static final int NTLMSSP_TARGET_TYPE_DOMAIN = 65536;
    public static final int NTLMSSP_NEGOTIATE_ALWAYS_SIGN = 32768;
    public static final int NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED = 8192;
    public static final int NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED = 4096;
    public static final int NTLMSSP_NEGOTIATE_ANONYMOUS = 2048;
    public static final int NTLMSSP_NEGOTIATE_NTLM = 512;
    public static final int NTLMSSP_NEGOTIATE_LM_KEY = 128;
    public static final int NTLMSSP_NEGOTIATE_DATAGRAM = 64;
    public static final int NTLMSSP_NEGOTIATE_SEAL = 32;
    public static final int NTLMSSP_NEGOTIATE_SIGN = 16;
    public static final int NTLMSSP_REQUEST_TARGET = 4;
    public static final int NTLMSSP_NEGOTIATE_OEM = 2;
    public static final int NTLMSSP_NEGOTIATE_UNICODE = 1;
    private static final Logger logger = Logger.getLogger(NegoFlags.class);
    public int value;

    public NegoFlags(int value) {
        this.value = value;
    }

    public NegoFlags() {
        this.value = 0;
    }

    public static void main(String[] args) {
        NegoFlags flags = new NegoFlags(-502758729);
        logger.info("Negotiation flags: " + flags);
    }

    public String toString() {
        return String.format("NegoFlags [value=0x%04x (%s)]", new Object[] { Integer.valueOf(this.value), flagsToSting() });
    }

    public String flagsToSting() {
        String str = "";

        if (NEGOTIATE_56())
            str = str + "NEGOTIATE_56 ";
        if (NEGOTIATE_KEY_EXCH())
            str = str + "NEGOTIATE_KEY_EXCH ";
        if (NEGOTIATE_128())
            str = str + "NEGOTIATE_128 ";
        if (NEGOTIATE_VERSION())
            str = str + "NEGOTIATE_VERSION ";
        if (NEGOTIATE_TARGET_INFO())
            str = str + "NEGOTIATE_TARGET_INFO ";
        if (REQUEST_NON_NT_SESSION_KEY())
            str = str + "REQUEST_NON_NT_SESSION_KEY ";
        if (NEGOTIATE_IDENTIFY())
            str = str + "NEGOTIATE_IDENTIFY ";
        if (NEGOTIATE_EXTENDED_SESSION_SECURITY())
            str = str + "NEGOTIATE_EXTENDED_SESSION_SECURITY ";
        if (TARGET_TYPE_SERVER())
            str = str + "TARGET_TYPE_SERVER ";
        if (TARGET_TYPE_DOMAIN())
            str = str + "TARGET_TYPE_DOMAIN ";
        if (NEGOTIATE_ALWAYS_SIGN())
            str = str + "NEGOTIATE_ALWAYS_SIGN ";
        if (NEGOTIATE_OEM_WORKSTATION_SUPPLIED())
            str = str + "NEGOTIATE_OEM_WORKSTATION_SUPPLIED ";
        if (NEGOTIATE_OEM_DOMAIN_SUPPLIED())
            str = str + "NEGOTIATE_OEM_DOMAIN_SUPPLIED ";
        if (NEGOTIATE_ANONYMOUS())
            str = str + "NEGOTIATE_ANONYMOUS ";
        if (NEGOTIATE_NTLM())
            str = str + "NEGOTIATE_NTLM ";
        if (NEGOTIATE_LM_KEY())
            str = str + "NEGOTIATE_LM_KEY ";
        if (NEGOTIATE_DATAGRAM())
            str = str + "NEGOTIATE_DATAGRAM ";
        if (NEGOTIATE_SEAL())
            str = str + "NEGOTIATE_SEAL ";
        if (NEGOTIATE_SIGN())
            str = str + "NEGOTIATE_SIGN ";
        if (REQUEST_TARGET())
            str = str + "REQUEST_TARGET ";
        if (NEGOTIATE_OEM())
            str = str + "NEGOTIATE_OEM ";
        if (NEGOTIATE_UNICODE()) {
            str = str + "NEGOTIATE_UNICODE ";
        }
        return str;
    }

    public boolean NEGOTIATE_56() {
        return (this.value & 0x80000000) != 0;
    }

    public boolean NEGOTIATE_KEY_EXCH() {
        return (this.value & 0x40000000) != 0;
    }

    public boolean NEGOTIATE_128() {
        return (this.value & 0x20000000) != 0;
    }

    public boolean NEGOTIATE_VERSION() {
        return (this.value & 0x2000000) != 0;
    }

    public boolean NEGOTIATE_TARGET_INFO() {
        return (this.value & 0x800000) != 0;
    }

    public boolean REQUEST_NON_NT_SESSION_KEY() {
        return (this.value & 0x400000) != 0;
    }

    public boolean NEGOTIATE_IDENTIFY() {
        return (this.value & 0x100000) != 0;
    }

    public boolean NEGOTIATE_EXTENDED_SESSION_SECURITY() {
        return (this.value & 0x80000) != 0;
    }

    public boolean TARGET_TYPE_SERVER() {
        return (this.value & 0x20000) != 0;
    }

    public boolean TARGET_TYPE_DOMAIN() {
        return (this.value & 0x10000) != 0;
    }

    public boolean NEGOTIATE_ALWAYS_SIGN() {
        return (this.value & 0x8000) != 0;
    }

    public boolean NEGOTIATE_OEM_WORKSTATION_SUPPLIED() {
        return (this.value & 0x2000) != 0;
    }

    public boolean NEGOTIATE_OEM_DOMAIN_SUPPLIED() {
        return (this.value & 0x1000) != 0;
    }

    public boolean NEGOTIATE_ANONYMOUS() {
        return (this.value & 0x800) != 0;
    }

    public boolean NEGOTIATE_NTLM() {
        return (this.value & 0x200) != 0;
    }

    public boolean NEGOTIATE_LM_KEY() {
        return (this.value & 0x80) != 0;
    }

    public boolean NEGOTIATE_DATAGRAM() {
        return (this.value & 0x40) != 0;
    }

    public boolean NEGOTIATE_SEAL() {
        return (this.value & 0x20) != 0;
    }

    public boolean NEGOTIATE_SIGN() {
        return (this.value & 0x10) != 0;
    }

    public boolean REQUEST_TARGET() {
        return (this.value & 0x4) != 0;
    }

    public boolean NEGOTIATE_OEM() {
        return (this.value & 0x2) != 0;
    }

    public boolean NEGOTIATE_UNICODE() {
        return (this.value & 0x1) != 0;
    }

    public NegoFlags set_NEGOTIATE_56() {
        this.value |= 0x80000000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_KEY_EXCH() {
        this.value |= 0x40000000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_128() {
        this.value |= 0x20000000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_VERSION() {
        this.value |= 0x2000000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_TARGET_INFO() {
        this.value |= 0x800000;
        return this;
    }

    public NegoFlags set_REQUEST_NON_NT_SESSION_KEY() {
        this.value |= 0x400000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_IDENTIFY() {
        this.value |= 0x100000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_EXTENDED_SESSION_SECURITY() {
        this.value |= 0x80000;
        return this;
    }

    public NegoFlags set_TARGET_TYPE_SERVER() {
        this.value |= 0x20000;
        return this;
    }

    public NegoFlags set_TARGET_TYPE_DOMAIN() {
        this.value |= 0x10000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_ALWAYS_SIGN() {
        this.value |= 0x8000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_OEM_WORKSTATION_SUPPLIED() {
        this.value |= 0x2000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_OEM_DOMAIN_SUPPLIED() {
        this.value |= 0x1000;
        return this;
    }

    public NegoFlags set_NEGOTIATE_ANONYMOUS() {
        this.value |= 0x800;
        return this;
    }

    public NegoFlags set_NEGOTIATE_NTLM() {
        this.value |= 0x200;
        return this;
    }

    public NegoFlags set_NEGOTIATE_LM_KEY() {
        this.value |= 0x80;
        return this;
    }

    public NegoFlags set_NEGOTIATE_DATAGRAM() {
        this.value |= 0x40;
        return this;
    }

    public NegoFlags set_NEGOTIATE_SEAL() {
        this.value |= 0x20;
        return this;
    }

    public NegoFlags set_NEGOTIATE_SIGN() {
        this.value |= 0x10;
        return this;
    }

    public NegoFlags set_REQUEST_TARGET() {
        this.value |= 0x4;
        return this;
    }

    public NegoFlags set_NEGOTIATE_OEM() {
        this.value |= 0x2;
        return this;
    }

    public NegoFlags set_NEGOTIATE_UNICODE() {
        this.value |= 0x1;
        return this;
    }
}
