package rdp.gold.brute;

import java.text.NumberFormat;

public class Memory {
    public static String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();

        sb.append("free memory: " + format.format(freeMemory / 1024L) + "\r\n");
        sb.append("allocated memory: " + format.format(allocatedMemory / 1024L) + "\r\n");
        sb.append("max memory: " + format.format(maxMemory / 1024L) + "\r\n");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024L) + "\r\n");

        return sb.toString();
    }
}
