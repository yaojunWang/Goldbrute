package rdp.gold.brute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class RDP {
    public static final byte RDP_NEG_RESP = 2;
    public static final byte RDP_NEG_FAILURE = 3;
    private static Map<Integer, String> protocols = new HashMap();

    private static Map<Byte, String> protocolFlags = new HashMap();

    static {
        protocols.put(Integer.valueOf(0), "PROTOCOL_RDP");
        protocols.put(Integer.valueOf(1), "PROTOCOL_SSL");
        protocols.put(Integer.valueOf(2), "PROTOCOL_HYBRID");
        protocols.put(Integer.valueOf(8), "PROTOCOL_HYBRID_EX");

        protocolFlags.put(Byte.valueOf((byte) 1), "EXTENDED_CLIENT_DATA_SUPPORTED");
        protocolFlags.put(Byte.valueOf((byte) 2), "DYNVC_GFX_PROTOCOL_SUPPORTED");
        protocolFlags.put(Byte.valueOf((byte) 4), "NEGRSP_FLAG_RESERVED");
        protocolFlags.put(Byte.valueOf((byte) 8), "RESTRICTED_ADMIN_MODE_SUPPORTED");
        protocolFlags.put(Byte.valueOf((byte) 16), "REDIRECTED_AUTHENTICATION_MODE_SUPPORTED");
    }

    public static String getProtocolName(int protocol) {
        if (protocols.containsKey(Integer.valueOf(protocol))) {
            return (String) protocols.get(Integer.valueOf(protocol));
        }

        return null;
    }

    public static List<String> getProtocolFlags(byte flags) {
        List<String> strFlags = new ArrayList();

        for (Map.Entry<Byte, String> entryFlag : protocolFlags.entrySet()) {
            if ((flags & ((Byte) entryFlag.getKey()).byteValue()) == ((Byte) entryFlag.getKey()).byteValue()) {
                strFlags.add(entryFlag.getValue());
            }
        }

        return strFlags;
    }
}
