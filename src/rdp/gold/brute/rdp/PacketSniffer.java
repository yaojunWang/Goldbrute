package rdp.gold.brute.rdp;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class PacketSniffer {
    public static final String SERVER_X224_CONNECTION_REQUEST = "Server X224ConnectionRequest";
    protected static final Pair serverX224ConnectionRequest = new Pair("Server X224ConnectionRequest", "03 00 XX XX 0E D0");
    private final Logger logger = Logger.getLogger(PacketSniffer.class);
    protected Pair[] regexps = null;

    public PacketSniffer(String id, Pair[] regexps) {
        this.regexps = regexps;
    }

    public void handleData(ByteBuffer buf) {
        matchPacket(buf);
    }

    public boolean matchX224ConnectionRequest(ByteBuffer buf) {
        String header = buf.toPlainHexString(19);
        return serverX224ConnectionRequest.regexp.matcher(header).find();
    }

    private void matchPacket(ByteBuffer buf) {
        String header = buf.toPlainHexString(100);
        for (Pair pair : this.regexps) {
            if (pair.regexp.matcher(header).find()) {
                this.logger.info("Packet: " + pair.name + ".");
                return;
            }
        }

        this.logger.info("Unknown packet: " + header + ".");
    }

    protected static class Pair {
        String name;
        Pattern regexp;

        protected Pair(String name, String regexp) {
            this.name = name;
            this.regexp = Pattern.compile("^" + replaceShortcuts(regexp), 2);
        }

        private static String replaceShortcuts(String regexp) {
            String result = regexp;
            result = result.replaceAll("XX\\*", "([0-9a-fA-F]{2} )*?");
            result = result.replaceAll("XX\\?", "([0-9a-fA-F]{2} )?");
            result = result.replaceAll("XX", "[0-9a-fA-F]{2}");
            return result;
        }
    }
}
