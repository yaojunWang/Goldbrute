package rdp.gold.brute.rdp.Messages.common.asn1;

import rdp.gold.brute.rdp.ByteBuffer;

public class BitString extends Tag {
    public ByteBuffer value;

    public BitString(String name) {
        super(name);
        this.tagType = 3;
    }

    public boolean isValueSet() {
        return this.value != null;
    }

    public long calculateLengthOfValuePayload() {
        return this.value.length;
    }

    public void writeTagValuePayload(ByteBuffer buf) {
        buf.writeBytes(this.value);
    }

    public void readTagValue(ByteBuffer buf, BerType typeAndFlags) {
        long length = buf.readBerLength();

        this.value = buf.readBytes((int) length);
    }

    public Tag deepCopy(String suffix) {
        return new BitString(this.name + suffix).copyFrom(this);
    }

    public Tag copyFrom(Tag tag) {
        super.copyFrom(tag);
        this.value = new ByteBuffer(((BitString) tag).value.toByteArray());
        return this;
    }
}
