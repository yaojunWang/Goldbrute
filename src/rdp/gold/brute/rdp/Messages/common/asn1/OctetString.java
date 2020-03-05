package rdp.gold.brute.rdp.Messages.common.asn1;

import rdp.gold.brute.rdp.ByteBuffer;

public class OctetString extends Tag {
    public ByteBuffer value = null;

    public OctetString(String name) {
        super(name);
        this.tagType = 4;
    }

    public void readTagValue(ByteBuffer buf, BerType typeAndFlags) {
        long length = buf.readBerLength();

        if (length > buf.length) {
            throw new RuntimeException("BER octet string is too long: " + length + " bytes. Data: " + buf + ".");
        }
        this.value = buf.readBytes((int) length);
    }

    public Tag deepCopy(String suffix) {
        return new OctetString(this.name + suffix).copyFrom(this);
    }

    public Tag copyFrom(Tag tag) {
        super.copyFrom(tag);
        this.value = ((OctetString) tag).value;
        return this;
    }

    public String toString() {
        return super.toString() + "= " + this.value;
    }

    public long calculateLengthOfValuePayload() {
        if (this.value != null) {
            return this.value.length;
        }
        return 0L;
    }

    public void writeTagValuePayload(ByteBuffer buf) {
        if (this.value != null) {
            buf.writeBytes(this.value);
        } else {
        }
    }

    public boolean isValueSet() {
        return this.value != null;
    }
}
