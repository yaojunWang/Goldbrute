package rdp.gold.brute.rdp;

public class BufferPool {
    public static byte[] allocateNewBuffer(int minSize) {
        if (minSize >= 0) {
            return new byte[minSize];
        }

        return new byte[131072];
    }

    public static void recycleBuffer(byte[] buf) {
    }
}
