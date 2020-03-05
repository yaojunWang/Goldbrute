package rdp.gold.brute.rdp.Messages.common.asn1;

import rdp.gold.brute.rdp.ByteBuffer;

public class Asn1Integer extends Tag {
    public Long value = null;

    public Asn1Integer(String name) {
        super(name);
        this.tagType = 2;
    }

    public void readTagValue(ByteBuffer buf, BerType typeAndFlags) {
        long length = buf.readBerLength();
        if (length > 8L) {
            throw new RuntimeException("[" + this + "] ERROR: Integer value is too long: " + length + " bytes. Cannot handle integers more than 8 bytes long. Data: " + buf + ".");
        }

        this.value = Long.valueOf(buf.readSignedVarInt((int) length));
    }

    public Tag deepCopy(String suffix) {
        return new Asn1Integer(this.name + suffix).copyFrom(this);
    }

    public Tag copyFrom(Tag tag) {
        super.copyFrom(tag);
        this.value = ((Asn1Integer) tag).value;
        return this;
    }

    public String toString() {
        return super.toString() + "= " + this.value;
    }

    public long calculateLengthOfValuePayload() {
        if (this.value.longValue() <= 255L)
            return 1L;
        if (this.value.longValue() <= 65535L)
            return 2L;
        if (this.value.longValue() <= 16777215L)
            return 3L;
        if (this.value.longValue() <= 4294967295L)
            return 4L;
        if (this.value.longValue() <= 1099511627775L)
            return 5L;
        if (this.value.longValue() <= 281474976710655L)
            return 6L;
        if (this.value.longValue() <= 72057594037927935L) {
            return 7L;
        }
        return 8L;
    }

    public void writeTagValuePayload(ByteBuffer buf) {
        long value = this.value.longValue();

        if (value < 255L) {
            buf.writeByte((int) value);
        } else if (value <= 65535L) {
            buf.writeShort((int) value);
        } else if (value <= 16777215L) {
            buf.writeByte((int) (value >> 16));
            buf.writeShort((int) value);
        } else if (value <= 4294967295L) {
            buf.writeInt((int) value);
        } else if (value <= 1099511627775L) {
            buf.writeByte((int) (value >> 32));
            buf.writeInt((int) value);
        } else if (value <= 281474976710655L) {
            buf.writeShort((int) (value >> 32));
            buf.writeInt((int) value);
        } else if (value <= 72057594037927935L) {
            buf.writeByte((int) (value >> 48));
            buf.writeShort((int) (value >> 32));
            buf.writeInt((int) value);
        } else {
            buf.writeInt((int) (value >> 32));
            buf.writeInt((int) value);
        }
    }

    public boolean isValueSet() {
        return this.value != null;
    }
}
