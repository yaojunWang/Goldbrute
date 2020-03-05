package rdp.gold.brute.utils;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;

public class ChecksumFile {
    @SuppressWarnings("resource")
    public static long checksumMappedFile(java.io.File file) throws java.io.IOException {
        FileInputStream inputStream = new FileInputStream(file);
        FileChannel fileChannel = inputStream.getChannel();

        int len = (int) fileChannel.size();

        MappedByteBuffer buffer = fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0L, len);
        CRC32 crc = new CRC32();

        for (int cnt = 0; cnt < len; cnt++) {
            int i = buffer.get(cnt);
            crc.update(i);
        }

        return crc.getValue();
    }
}
