package rdp.gold.brute.rdp.Messages.common.asn1;

import rdp.gold.brute.rdp.ByteBuffer;

public class Any extends Tag {
    public ByteBuffer value;

    public Any(String name) {
        super(name);
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
        return new Any(this.name + suffix).copyFrom(this);
    }

    public Tag copyFrom(Tag tag) {
        super.copyFrom(tag);
        this.tagType = tag.tagType;
        this.value = new ByteBuffer(((Any) tag).value.toByteArray());
        return this;
    }

    public boolean isTypeValid(BerType typeAndFlags, boolean explicit) {
        if (explicit) {
            return (typeAndFlags.tagClass == this.tagClass) && (typeAndFlags.constructed) && (typeAndFlags.typeOrTagNumber == this.tagNumber);
        }
        return true;
    }
}
