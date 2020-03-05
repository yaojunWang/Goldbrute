package rdp.gold.brute.utils;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.log4j.Logger;

public final class DataTransformer {
    private static LZ4Factory factory = LZ4Factory.fastestJavaInstance();
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass());

    public static byte[] compressBytes(byte[] packet) throws Exception {
        int decompressedLength = packet.length;
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        compressor.compress(packet, 0, decompressedLength, compressed, 0, maxCompressedLength);

        return compressed;
    }

    public static byte[] decompressBytes(byte[] packet, int packetLength) throws Exception {
        LZ4FastDecompressor decompressor = factory.fastDecompressor();
        byte[] decompressedPacket = new byte[packetLength];
        decompressor.decompress(packet, 0, decompressedPacket, 0, packetLength);

        return decompressedPacket;
    }
}
