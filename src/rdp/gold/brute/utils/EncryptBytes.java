package rdp.gold.brute.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import rdp.gold.brute.Config;

public class EncryptBytes {
    private static final Logger logger = Logger.getLogger(EncryptBytes.class);
    private static Cipher cipherEncrypt;
    private static Cipher cipherDecrypt;

    static {
        try {
            SecretKeySpec key = new SecretKeySpec(Config.KEY_ENCRYPT, "AES");
            IvParameterSpec iv = new IvParameterSpec(Config.IV_ENCRYPT);

            cipherEncrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherEncrypt.init(1, key, iv);

            cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherDecrypt.init(2, key, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(e + " " + sw);
            System.err.println(e.getMessage());
            e.printStackTrace();

            System.exit(0);
        }
    }

    public static byte[] encryptBytes(byte[] bytesArray) throws Exception {
        return cipherEncrypt.doFinal(bytesArray);
    }

    public static byte[] decryptBytes(byte[] bytesArray) throws Exception {
        return cipherDecrypt.doFinal(bytesArray);
    }
}
