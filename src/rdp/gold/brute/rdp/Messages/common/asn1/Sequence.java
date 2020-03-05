package rdp.gold.brute.rdp.Messages.common.asn1;

import java.util.Arrays;
import rdp.gold.brute.rdp.ByteBuffer;

public class Sequence extends Tag {
    public Tag[] tags;

    public Sequence(String name) {
        super(name);
        this.tagType = 16;

        this.constructed = true;
    }

    public long calculateLengthOfValuePayload() {
        long sum = 0L;

        for (Tag tag : this.tags) {
            long tagLength = tag.calculateFullLength();
            sum += tagLength;
        }

        return sum;
    }

    public void writeTagValuePayload(ByteBuffer buf) {
        for (Tag tag : this.tags) {
            tag.writeTag(buf);
        }
    }

    public void readTagValue(ByteBuffer buf, BerType typeAndFlags) {
        long length = buf.readBerLength();
        if (length > buf.remainderLength()) {
            throw new RuntimeException("BER sequence is too long: " + length + " bytes, while buffer remainder length is " + buf.remainderLength() + ". Data: " + buf + ".");
        }

        ByteBuffer value = buf.readBytes((int) length);
        parseContent(value);

        value.unref();
    }

    protected void parseContent(ByteBuffer buf) {
        for (int i = 0; (buf.remainderLength() > 0) && (i < this.tags.length); i++) {
            BerType typeAndFlags = readBerType(buf);

            if (!this.tags[i].isTypeValid(typeAndFlags)) {

                if (!this.tags[i].optional) {
                    throw new RuntimeException("[" + this + "] ERROR: Required tag is missed: " + this.tags[i] + ". Unexected tag type: " + typeAndFlags + ". Data: " + buf + ".");
                }
                for (;

                        i < this.tags.length; i++) {
                    if (this.tags[i].isTypeValid(typeAndFlags)) {
                        break;
                    }
                }

                if ((i >= this.tags.length) || (!this.tags[i].isTypeValid(typeAndFlags))) {
                    throw new RuntimeException("[" + this + "] ERROR: No more tags to read or skip, but some data still left in buffer. Unexected tag type: " + typeAndFlags + ". Data: " + buf + ".");
                }
            }

            this.tags[i].readTag(buf, typeAndFlags);
        }
    }

    public boolean isTypeValid(BerType typeAndFlags, boolean explicit) {
        if (explicit) {
            return (typeAndFlags.tagClass == this.tagClass) && (typeAndFlags.constructed) && (typeAndFlags.typeOrTagNumber == this.tagNumber);
        }

        return (typeAndFlags.tagClass == 0) && (typeAndFlags.constructed) && (typeAndFlags.typeOrTagNumber == 16);
    }

    public Tag deepCopy(String suffix) {
        return new Sequence(this.name + suffix).copyFrom(this);
    }

    public Tag copyFrom(Tag tag) {
        super.copyFrom(tag);

        if (this.tags.length != ((Sequence) tag).tags.length) {
            throw new RuntimeException("Incompatible sequences. This: " + this + ", another: " + tag + ".");
        }
        for (int i = 0; i < this.tags.length; i++) {
            this.tags[i].copyFrom(((Sequence) tag).tags[i]);
        }

        return this;
    }

    public String toString() {
        return super.toString() + "{" + Arrays.toString(this.tags) + " }";
    }

    public boolean isValueSet() {
        return this.tags != null;
    }
}
