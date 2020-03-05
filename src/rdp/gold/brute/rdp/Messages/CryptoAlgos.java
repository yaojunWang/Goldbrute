package rdp.gold.brute.rdp.Messages;

import java.lang.reflect.Method;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import rdp.gold.brute.rdp.RdpConstants;

public class CryptoAlgos implements NtlmConstants {
    public static final String NIL = "";

    public static String concatenationOf(String... args) {
        StringBuffer sb = new StringBuffer();
        for (String arg : args) {
            sb.append(arg);
        }
        return sb.toString();
    }

    public static byte[] concatenationOf(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int destPos = 0;
        for (int index = 0; index < arrays.length; index++) {
            byte[] array = arrays[index];
            System.arraycopy(array, 0, result, destPos, array.length);
            destPos += array.length;
        }
        return result;
    }

    public static byte[] CRC32(byte[] m) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] DES(byte[] k, byte[] d) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] DESL(byte[] k, byte[] d) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] getVersion() {
        return new byte[] { 6, 1, -79, 29, 0, 0, 0, 15 };
    }

    public static byte[] LMGETKEY(byte[] u, byte[] d) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] NTGETKEY(byte[] u, byte[] d) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] HMAC(byte[] k, byte[] m) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] HMAC_MD5(byte[] k, byte[] m) {
        try {
            String algorithm = "HMacMD5";
            Mac hashMac = Mac.getInstance(algorithm);

            Key secretKey = new SecretKeySpec(k, 0, k.length, algorithm);
            hashMac.init(secretKey);
            return hashMac.doFinal(m);
        } catch (Exception e) {
            throw new RuntimeException("Cannot calculate HMAC-MD5.", e);
        }
    }

    public static byte[] KXKEY(byte[] sessionBaseKey) {
        return Arrays.copyOf(sessionBaseKey, sessionBaseKey.length);
    }

    public static byte[] LMOWF() {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] MD4(byte[] m) {
        try {
            MD4 md4 = new MD4();
            md4.update(m);
            return md4.digest();
        } catch (Exception e) {
            throw new RuntimeException("Cannot calculate MD5.", e);
        }
    }

    public static byte[] MD5(byte[] m) {
        try {
            return MessageDigest.getInstance("MD5").digest(m);
        } catch (Exception e) {
            throw new RuntimeException("Cannot calculate MD5.", e);
        }
    }

    public static byte[] MD5_HASH(byte[] m) {
        try {
            return MessageDigest.getInstance("MD5").digest(m);
        } catch (Exception e) {
            throw new RuntimeException("Cannot calculate MD5.", e);
        }
    }

    public static byte[] NONCE(int n) {
        byte[] nonce = new byte[n];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);

        return nonce;
    }

    public static byte[] NTOWF() {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] RC4(Cipher h, byte[] d) {
        return h.update(d);
    }

    public static byte[] RC4K(byte[] k, byte[] d) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            Key key = new SecretKeySpec(k, "RC4");
            cipher.init(1, key);
            return cipher.doFinal(d);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Cipher RC4Init(byte[] k) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] SEALKEY(byte[] f, byte[] k, byte[] string1) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] SIGNKEY(int flag, byte[] k, byte[] string1) {
        throw new RuntimeException("FATAL: Not implemented.");
    }

    public static byte[] Currenttime() {
        long time = (System.currentTimeMillis() + 11644473600000L) * 10000L;

        byte[] result = new byte[8];
        for (int i = 0; i < 8; time >>>= 8) {
            result[i] = ((byte) (int) time);
            i++;
        }
        return result;
    }

    public static byte[] UNICODE(String string) {
        return string.getBytes(RdpConstants.CHARSET_16);
    }

    public static String UpperCase(String string) {
        return string.toUpperCase();
    }

    public static byte[] Z(int n) {
        return new byte[n];
    }

    public static Cipher initRC4(byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(1, new SecretKeySpec(key, "RC4"));
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize RC4 sealing handle with client sealing key.", e);
        }
    }

    public static void callAll(Object obj) {
        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("test")) {
                try {
                    m.invoke(obj, (Object[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        callAll(new CryptoAlgos());
    }
}
