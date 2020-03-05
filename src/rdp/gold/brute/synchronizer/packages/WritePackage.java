package rdp.gold.brute.synchronizer.packages;

import java.nio.ByteBuffer;

public abstract interface WritePackage {
    public abstract ByteBuffer getPackage() throws Exception;

    public abstract Class<?> getAwaitPackage();

    public abstract Class<?> getWritePackage();
}
