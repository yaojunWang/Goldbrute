package rdp.gold.brute.rdp;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ByteBuffer {
    public static final String SEQUENCE_NUMBER = "seq";
    public byte[] data;
    public int offset = 0;
    public int length = 0;
    public int cursor = 0;
    private int refCount = 1;
    private Order order;
    private Map<String, Object> metadata = null;

    public ByteBuffer(int minLength) {
        this.data = BufferPool.allocateNewBuffer(minLength);
        this.offset = 0;
        this.length = minLength;
    }

    public ByteBuffer(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data must be non-null.");
        }
        this.data = data;
        this.offset = 0;
        this.length = data.length;
    }

    public ByteBuffer(byte[] data, int offset, int length) {
        if (data == null) {
            throw new NullPointerException("Data must be non-null.");
        }
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    public ByteBuffer(int minLength, boolean reserveSpaceForHeader) {
        this.data = BufferPool.allocateNewBuffer(128 + minLength);
        this.offset = 128;
        this.length = minLength;
    }

    public ByteBuffer(Order order) {
        this.order = order;
    }

    public static ByteBuffer[] convertByteArraysToByteBuffers(byte[]... bas) {
        ByteBuffer[] bufs = new ByteBuffer[bas.length];

        int i = 0;
        for (byte[] ba : bas) {
            bufs[(i++)] = new ByteBuffer(ba);
        }
        return bufs;
    }

    protected static long calculateUnsignedInt(byte value1, byte value2, byte value3, byte value4) {
        return (calculateUnsignedByte(value1) << 24) + (calculateUnsignedByte(value2) << 16) + (calculateUnsignedByte(value3) << 8) + calculateUnsignedByte(value4);
    }

    protected static int calculateUnsignedByte(byte value) {
        return value & 0xFF;
    }

    protected static int calculateUnsignedShort(byte value1, byte value2) {
        return calculateUnsignedByte(value1) << 8 | calculateUnsignedByte(value2);
    }

    protected static short calculateSignedShort(byte value1, byte value2) {
        return (short) calculateUnsignedShort(value1, value2);
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String toString() {
        return toString(this.length);
    }

    public String toString(int maxLength) {
        return "ByteRange(){offset=" + this.offset + ", length=" + this.length + ", cursor=" + this.cursor + ", data=" + (this.data == null ? "null" : toHexString(maxLength))
                + ((this.metadata == null) || (this.metadata.size() == 0) ? "" : new StringBuilder().append(", metadata=").append(this.metadata).toString()) + "}";
    }

    public String toHexString(int maxLength) {
        StringBuilder builder = new StringBuilder(maxLength * 6);
        builder.append('[');
        for (int i = 0; (i < maxLength) && (i < this.length); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            int b = this.data[(this.offset + i)] & 0xFF;
            builder.append("0x" + (b < 16 ? "0" : "") + Integer.toString(b, 16));
        }
        builder.append(']');
        return builder.toString();
    }

    public String toPlainHexString(int maxLength) {
        StringBuilder builder = new StringBuilder(maxLength * 3);
        for (int i = 0; (i < maxLength) && (i < this.length); i++) {
            if (i > 0) {
                builder.append(" ");
            }
            int b = this.data[(this.offset + i)] & 0xFF;
            builder.append(String.format("%02x", new Object[] { Integer.valueOf(b) }));
        }
        return builder.toString();
    }

    public String dump() {
        StringBuilder builder = new StringBuilder(this.length * 4);
        int i = addBytesToBuilder(builder);
        int end = i - 1;
        if (end % 16 != 15) {
            int begin = end & 0xFFFFFFF0;
            for (int j = 0; j < 15 - end % 16; j++) {
                builder.append("   ");
            }
            builder.append(' ');
            builder.append(toASCIIString(begin, end));
            builder.append('\n');
        }
        return builder.toString();
    }

    protected int addBytesToBuilder(StringBuilder builder) {
        for (int i = 0; i < this.length; i++) {
            if (i % 16 == 0) {
                builder.append(String.format("%04x", new Object[] { Integer.valueOf(i) }));
            }
            builder.append(' ');
            int b = this.data[(this.offset + i)] & 0xFF;
            builder.append(String.format("%02x", new Object[] { Integer.valueOf(b) }));
            if (i % 16 == 15) {
                builder.append(' ');
                builder.append(toASCIIString(i - 15, i));
                builder.append('\n');
            }
        }
        return builder.length();
    }

    private String toASCIIString(int start, int finish) {
        StringBuffer sb = new StringBuffer(16);
        for (int i = start; i <= finish; i++) {
            char ch = (char) this.data[(this.offset + i)];
            if ((ch < ' ') || (ch >= '')) {
                sb.append('.');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public String toPlainHexString() {
        return toPlainHexString(this.length);
    }

    public void extend(int newLength) {
        if (this.data.length < newLength) {
            Arrays.copyOf(this.data, newLength);
        }
    }

    public void ref() {
        this.refCount += 1;
    }

    public void unref() {
        this.refCount -= 1;
        if (this.refCount == 0) {
            BufferPool.recycleBuffer(this.data);

            this.data = null;
        }
    }

    public boolean isSoleOwner() {
        return this.refCount == 1;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ByteBuffer slice(int offset, int length, boolean copyMetadata) {
        ref();
        if (this.length < offset + length) {
            throw new RuntimeException("Length of region is larger that length of this buffer. Buffer length: " + this.length + ", offset: " + offset + ", new region length: " + length + ".");
        }
        ByteBuffer slice = new ByteBuffer(this.data, this.offset + offset, length);
        if ((copyMetadata) && (this.metadata != null)) {
            slice.metadata = new HashMap(this.metadata);
        }
        return slice;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object putMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap();
        }
        return this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ByteBuffer join(ByteBuffer buf) {
        int newLength = this.length + buf.length;
        byte[] newData = new byte[newLength];

        System.arraycopy(this.data, this.offset, newData, 0, this.length);

        System.arraycopy(buf.data, buf.offset, newData, this.length, buf.length);

        ByteBuffer newBuf = new ByteBuffer(newData);
        if (this.metadata != null) {
            newBuf.metadata = new HashMap(this.metadata);
        }
        return newBuf;
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(this.data, this.offset, this.offset + this.length);
    }

    public short[] toShortArray() {
        if (this.length % 2 != 0) {
            throw new ArrayIndexOutOfBoundsException("Length of byte array must be dividable by 2 without remainder. Array length: " + this.length + ", remainder: " + this.length % 2 + ".");
        }
        short[] buf = new short[this.length / 2];

        int i = 0;
        for (int j = this.offset; i < buf.length; j += 2) {
            buf[i] = ((short) (this.data[(j + 0)] & 0xFF | (this.data[(j + 1)] & 0xFF) << 8));
            i++;
        }
        return buf;
    }

    public int[] toIntLEArray() {
        if (this.length % 4 != 0) {
            throw new ArrayIndexOutOfBoundsException("Length of byte array must be dividable by 4 without remainder. Array length: " + this.length + ", remainder: " + this.length % 4 + ".");
        }
        int[] buf = new int[this.length / 4];

        int i = 0;
        for (int j = this.offset; i < buf.length; j += 4) {
            buf[i] = (this.data[(j + 0)] & 0xFF | (this.data[(j + 1)] & 0xFF) << 8 | (this.data[(j + 2)] & 0xFF) << 16 | (this.data[(j + 3)] & 0xFF) << 24);
            i++;
        }
        return buf;
    }

    public int[] toInt3LEArray() {
        if (this.length % 3 != 0) {
            throw new ArrayIndexOutOfBoundsException("Length of byte array must be dividable by 3 without remainder. Array length: " + this.length + ", remainder: " + this.length % 3 + ".");
        }
        int[] buf = new int[this.length / 3];

        int i = 0;
        for (int j = this.offset; i < buf.length; j += 3) {
            buf[i] = (this.data[(j + 0)] & 0xFF | (this.data[(j + 1)] & 0xFF) << 8 | (this.data[(j + 2)] & 0xFF) << 16);
            i++;
        }
        return buf;
    }

    public int readSignedInt() {
        if (this.cursor + 4 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 4 bytes from this buffer: " + this + ".");
        }
        int result = ((this.data[(this.offset + this.cursor)] & 0xFF) << 24) + ((this.data[(this.offset + this.cursor + 1)] & 0xFF) << 16) + ((this.data[(this.offset + this.cursor + 2)] & 0xFF) << 8)
                + (this.data[(this.offset + this.cursor + 3)] & 0xFF);
        this.cursor += 4;
        return result;
    }

    public int readSignedIntLE() {
        if (this.cursor + 4 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 4 bytes from this buffer: " + this + ".");
        }
        int result = ((this.data[(this.offset + this.cursor + 3)] & 0xFF) << 24) + ((this.data[(this.offset + this.cursor + 2)] & 0xFF) << 16)
                + ((this.data[(this.offset + this.cursor + 1)] & 0xFF) << 8) + (this.data[(this.offset + this.cursor)] & 0xFF);
        this.cursor += 4;
        return result;
    }

    public long readUnsignedIntLE() {
        if (this.cursor + 4 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 4 bytes from this buffer: " + this + ".");
        }
        long result = ((this.data[(this.offset + this.cursor + 3)] & 0xFF) << 24) + ((this.data[(this.offset + this.cursor + 2)] & 0xFF) << 16)
                + ((this.data[(this.offset + this.cursor + 1)] & 0xFF) << 8) + (this.data[(this.offset + this.cursor + 0)] & 0xFF);

        this.cursor += 4;
        return result;
    }

    public long readUnsignedInt() {
        if (this.cursor + 4 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 4 bytes from this buffer: " + this + ".");
        }
        byte value1 = this.data[(this.offset + this.cursor + 0)];
        byte value2 = this.data[(this.offset + this.cursor + 1)];
        byte value3 = this.data[(this.offset + this.cursor + 2)];
        byte value4 = this.data[(this.offset + this.cursor + 3)];
        long result = calculateUnsignedInt(value1, value2, value3, value4);
        this.cursor += 4;
        return result;
    }

    public int readVariableSignedIntLE() {
        int result = 0;
        for (int shift = 0; shift < 32; shift += 7) {
            int b = readUnsignedByte();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return result;
    }

    public int readEncodedUnsignedInt() {
        int firstByte = readUnsignedByte();
        int result;
        switch (firstByte & 0xC0) {
        case 0:
        default:
            result = firstByte & 0x3F;
            break;
        case 64:
            result = firstByte & 0x3F00 | readUnsignedByte();
            break;
        case 128:
            result = (firstByte & 0x3F00 | readUnsignedByte()) << 8 | readUnsignedByte();
            break;
        case 192:
            result = (firstByte & 0x3F00 | readUnsignedByte()) << 8 | readUnsignedByte() << 8 | readUnsignedByte();
        }
        return result;
    }

    public int readUnsignedByte() {
        if (this.cursor + 1 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 1 byte from this buffer: " + this + ".");
        }
        byte value = this.data[(this.offset + this.cursor)];
        int b = calculateUnsignedByte(value);
        this.cursor += 1;
        return b;
    }

    public byte readSignedByte() {
        if (this.cursor + 1 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 1 byte from this buffer: " + this + ".");
        }
        byte b = this.data[(this.offset + this.cursor)];
        this.cursor += 1;
        return b;
    }

    public int readUnsignedShort() {
        if (this.cursor + 2 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 2 bytes from this buffer: " + this + ".");
        }
        byte value1 = this.data[(this.offset + this.cursor)];
        byte value2 = this.data[(this.offset + this.cursor + 1)];
        int result = calculateUnsignedShort(value1, value2);
        this.cursor += 2;
        return result;
    }

    public short readSignedShortLE() {
        if (this.cursor + 2 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 2 bytes from this buffer: " + this + ".");
        }
        short result = (short) ((this.data[(this.offset + this.cursor + 1)] & 0xFF) << 8 | this.data[(this.offset + this.cursor)] & 0xFF);
        this.cursor += 2;
        return result;
    }

    public short readSignedShort() {
        if (this.cursor + 2 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 2 bytes from this buffer: " + this + ".");
        }
        byte value1 = this.data[(this.offset + this.cursor + 0)];
        byte value2 = this.data[(this.offset + this.cursor + 1)];
        short result = calculateSignedShort(value1, value2);
        this.cursor += 2;
        return result;
    }

    public int readVariableUnsignedShort() {
        int firstByte = readUnsignedByte();
        int result;
        if ((firstByte & 0x80) == 0) {
            result = firstByte & 0x7F;
        } else {
            int secondByte = readUnsignedByte();
            result = (firstByte & 0x7F) << 8 | secondByte;
        }
        return result;
    }

    public long readBerLength() {
        int firstByte = readUnsignedByte();
        long result;
        if ((firstByte & 0x80) == 0) {
            result = firstByte & 0x7F;
        } else {
            int intLength = firstByte & 0x7F;
            if (intLength != 0) {
                result = readUnsignedVarInt(intLength);
            } else {
                return -1L;
            }
        }
        return result;
    }

    public void writeBerLength(long length) {
        if (length < 0L) {
            throw new RuntimeException("Length cannot be less than zero: " + length + ". Data: " + this + ".");
        }
        if (length < 128L) {
            writeByte((int) length);
        } else if (length < 255L) {
            writeByte(129);
            writeByte((int) length);
        } else if (length <= 65535L) {
            writeByte(130);
            writeShort((int) length);
        } else if (length <= 16777215L) {
            writeByte(131);
            writeByte((int) (length >> 16));
            writeShort((int) length);
        } else if (length <= 4294967295L) {
            writeByte(132);
            writeInt((int) length);
        } else if (length <= 1099511627775L) {
            writeByte(133);
            writeByte((int) (length >> 32));
            writeInt((int) length);
        } else if (length <= 281474976710655L) {
            writeByte(134);
            writeShort((int) (length >> 32));
            writeInt((int) length);
        } else if (length <= 72057594037927935L) {
            writeByte(135);
            writeByte((int) (length >> 48));
            writeShort((int) (length >> 32));
            writeInt((int) length);
        } else {
            writeByte(136);
            writeInt((int) (length >> 32));
            writeInt((int) length);
        }
    }

    public long readSignedVarInt(int len) {
        long value = 0L;
        switch (len) {
        case 0:
            value = 0L;
            break;
        case 1:
            value = readSignedByte();
            break;
        case 2:
            value = readSignedShort();
            break;
        case 3:
            value = readSignedByte() << 16 | readUnsignedShort();
            break;
        case 4:
            value = readSignedInt();
            break;
        case 5:
            value = readSignedByte() | readUnsignedInt();
            break;
        case 6:
            value = readSignedShort() | readUnsignedInt();
            break;
        case 7:
            value = readSignedByte() << 24 | readUnsignedShort() | readUnsignedInt();
            break;
        case 8:
            value = readSignedLong();
            break;
        default:
            throw new RuntimeException("Cannot read integers which are more than 8 bytes long. Length: " + len + ". Data: " + this + ".");
        }
        return value;
    }

    public long readUnsignedVarInt(int len) {
        long value = 0L;
        switch (len) {
        case 0:
            value = 0L;
            break;
        case 1:
            value = readUnsignedByte();
            break;
        case 2:
            value = readUnsignedShort();
            break;
        case 3:
            value = readUnsignedByte() << 16 | readUnsignedShort();
            break;
        case 4:
            value = readUnsignedInt();
            break;
        case 5:
            value = readUnsignedByte() | readUnsignedInt();
            break;
        case 6:
            value = readUnsignedShort() | readUnsignedInt();
            break;
        case 7:
            value = readUnsignedByte() << 16 | readUnsignedShort() | readUnsignedInt();
            break;
        case 8:
            value = readSignedLong();
            if (value < 0L) {
                throw new RuntimeException(
                        "Cannot read 64 bit integers which are larger than 0x7FffFFffFFffFFff, because of lack of unsinged long type in Java. Value: " + value + ". Data: " + this + ".");
            }
            break;
        default:
            throw new RuntimeException("Cannot read integers which are more than 8 bytes long. Length: " + len + ". Data: " + this + ".");
        }
        return value;
    }

    public int readUnsignedShortLE() {
        if (this.cursor + 2 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read 2 bytes from this buffer: " + this + ".");
        }
        int result = (this.data[(this.offset + this.cursor + 1)] & 0xFF) << 8 | this.data[(this.offset + this.cursor)] & 0xFF;
        this.cursor += 2;
        return result;
    }

    public int readEncodedUnsignedShort() {
        int firstByte = readUnsignedByte();
        int result;
        if ((firstByte & 0x80) == 0) {
            result = firstByte & 0x7F;
        } else {
            int secondByte = readUnsignedByte();
            result = (firstByte & 0x7F) << 8 | secondByte;
        }
        return result;
    }

    public int readEncodedSignedShort() {
        int firstByte = readUnsignedByte();
        int result;
        if ((firstByte & 0x80) == 0) {
            result = firstByte & 0x3F;
        } else {
            int secondByte = readUnsignedByte();
            result = (firstByte & 0x3F) << 8 | secondByte;
        }
        if ((firstByte & 0x40) > 0) {
            return -result;
        }
        return result;
    }

    public long readSignedLongLE() {
        return readSignedIntLE() & 0xFFFFFFFF | readSignedIntLE() << 32;
    }

    public long readSignedLong() {
        return readSignedInt() << 32 | readSignedInt() & 0xFFFFFFFF;
    }

    public String readString(int length, Charset charset) {
        if (this.cursor + length > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read " + length + " bytes from this buffer: " + this + ".");
        }
        String string = new String(this.data, this.offset + this.cursor, length, charset);
        this.cursor += length;
        return string;
    }

    public String readVariableString(Charset charset) {
        int start = this.cursor;
        while (readUnsignedByte() != 0) {
        }
        String string = new String(this.data, this.offset + start, this.cursor - start - 1, charset);

        return string;
    }

    public String readVariableWideString(Charset charset) {
        int start = this.cursor;
        while (readUnsignedShortLE() != 0) {
        }
        String string = new String(this.data, this.offset + start, this.cursor - start - 2, charset);

        return string;
    }

    public ByteBuffer readBytes(int dataLength) {
        if (this.cursor + dataLength > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read " + dataLength + " bytes from this buffer: " + this + ".");
        }
        ByteBuffer slice = slice(this.cursor, dataLength, false);
        this.cursor += dataLength;
        return slice;
    }

    public void skipBytes(int numOfBytes) {
        if (this.cursor + numOfBytes > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot read " + numOfBytes + " bytes from this buffer: " + this + ".");
        }
        this.cursor += numOfBytes;
    }

    public void writeByte(int b) {
        if (this.cursor + 1 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot write 1 byte to this buffer: " + this + ".");
        }
        this.data[(this.offset + this.cursor)] = ((byte) b);
        this.cursor += 1;
    }

    public void writeShort(int x) {
        if (this.cursor + 2 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot write 2 bytes to this buffer: " + this + ".");
        }
        this.data[(this.offset + this.cursor)] = ((byte) (x >> 8));
        this.data[(this.offset + this.cursor + 1)] = ((byte) x);
        this.cursor += 2;
    }

    public void writeShortLE(int x) {
        if (this.cursor + 2 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot write 2 bytes to this buffer: " + this + ".");
        }
        this.data[(this.offset + this.cursor + 1)] = ((byte) (x >> 8));
        this.data[(this.offset + this.cursor)] = ((byte) x);
        this.cursor += 2;
    }

    public void writeInt(int i) {
        if (this.cursor + 4 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot write 4 bytes to this buffer: " + this + ".");
        }
        this.data[(this.offset + this.cursor)] = ((byte) (i >> 24));
        this.data[(this.offset + this.cursor + 1)] = ((byte) (i >> 16));
        this.data[(this.offset + this.cursor + 2)] = ((byte) (i >> 8));
        this.data[(this.offset + this.cursor + 3)] = ((byte) i);
        this.cursor += 4;
    }

    public void writeIntLE(int i) {
        if (this.cursor + 4 > this.length) {
            throw new ArrayIndexOutOfBoundsException("Cannot write 4 bytes to this buffer: " + this + ".");
        }
        this.data[(this.offset + this.cursor)] = ((byte) i);
        this.data[(this.offset + this.cursor + 1)] = ((byte) (i >> 8));
        this.data[(this.offset + this.cursor + 2)] = ((byte) (i >> 16));
        this.data[(this.offset + this.cursor + 3)] = ((byte) (i >> 24));
        this.cursor += 4;
    }

    public void writeVariableIntLE(int i) {
        while (i != 0) {
            int b = i & 0x7F;
            i >>= 7;
            if (i > 0) {
                b |= 0x80;
            }
            writeByte(b);
        }
    }

    public void writeVariableShort(int length) {
        if (((length > 127 ? 1 : 0) | (length < 0 ? 1 : 0)) != 0) {
            writeShort(length | 0x8000);
        } else {
            writeByte(length);
        }
    }

    public void prepend(ByteBuffer buf) {
        prepend(buf.data, buf.offset, buf.length);
    }

    public void prepend(byte[] data) {
        prepend(data, 0, data.length);
    }

    public void prepend(byte[] data, int offset, int length) {
        if (!isSoleOwner()) {
            throw new RuntimeException("Create full copy of this byte buffer data for modification. refCount: " + this.refCount + ".");
        }
        if (this.offset < length) {
            throw new RuntimeException("Reserve data to have enough space for header.");
        }
        System.arraycopy(data, offset, this.data, this.offset - length, length);

        this.offset -= length;
        this.length += length;
        this.cursor += length;
    }

    public void writeString(String str, Charset charset) {
        writeBytes(str.getBytes(charset));
    }

    public void writeFixedString(int length, String str, Charset charset) {
        byte[] bytes = str.getBytes(charset);
        writeBytes(bytes, 0, Math.min(bytes.length, length));
        for (int i = bytes.length; i < length; i++) {
            writeByte(0);
        }
    }

    public void writeBytes(ByteBuffer buf) {
        writeBytes(buf.data, buf.offset, buf.length);
    }

    public void writeBytes(byte[] bytes) {
        writeBytes(bytes, 0, bytes.length);
    }

    public void writeBytes(byte[] bytes, int offset, int length) {
        System.arraycopy(bytes, offset, this.data, this.offset + this.cursor, length);
        this.cursor += length;
    }

    public void trimAtCursor() {
        this.length = this.cursor;
    }

    public void rewindCursor() {
        this.cursor = 0;
    }

    public int readRGBColor() {
        return readUnsignedByte() | readUnsignedByte() << 8 | readUnsignedByte() << 16;
    }

    public void assertThatBufferIsFullyRead() {
        if (this.cursor != this.length) {
            throw new RuntimeException("Data in buffer is not read fully. Buf: " + this + ".");
        }
    }

    public int hashCode() {
        int result = 1;

        int end = this.offset + this.length;
        for (int i = this.offset; i < end; i++) {
            result = 31 * result + this.data[i];
        }
        result = 31 * result + this.length;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ByteBuffer other = (ByteBuffer) obj;
        if (this.length != other.length) {
            return false;
        }
        for (int i = 0; i < this.length; i++) {
            if (this.data[(this.offset + i)] != other.data[(other.offset + i)]) {
                return false;
            }
        }
        return true;
    }

    public int remainderLength() {
        if (this.length >= this.cursor) {
            return this.length - this.cursor;
        }
        throw new RuntimeException("Inconsistent state of buffer: cursor is after end of buffer: " + this + ".");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<String> getMetadataKeys() {
        if (this.metadata != null) {
            return this.metadata.keySet();
        }
        return new HashSet(0);
    }

    public int peekUnsignedByte(int i) {
        return this.data[(this.offset + this.cursor + i)] & 0xFF;
    }

    public void trimHeader(int length) {
        this.offset += length;
        this.length -= length;
        rewindCursor();
    }
}
