package rdp.gold.brute.rdp;

import java.nio.charset.Charset;

public class ConstantTimeComparator {
    public static boolean compareBytes(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < b1.length; i++) {
            result |= b1[i] ^ b2[i];
        }
        return result == 0;
    }

    public static boolean compareStrings(String s1, String s2) {
        Charset encoding = Charset.forName("UTF-8");
        return compareBytes(s1.getBytes(encoding), s2.getBytes(encoding));
    }
}
