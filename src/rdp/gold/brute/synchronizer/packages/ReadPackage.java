package rdp.gold.brute.synchronizer.packages;

public abstract interface ReadPackage {
    public abstract void processPacket(byte[] paramArrayOfByte) throws Exception;

    public abstract Class<?> getAwaitPackage();

    public abstract Class<?> getWritePackage();
}
