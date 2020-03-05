package rdp.gold.brute.rdp.Messages.common.asn1;

import java.util.ArrayList;
import rdp.gold.brute.rdp.ByteBuffer;

public class SequenceOf extends Sequence {
    public Tag type;

    public SequenceOf(String name) {
        super(name);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void parseContent(ByteBuffer buf) {
        ArrayList<Tag> tagList = new ArrayList();

        for (int index = 0; buf.remainderLength() > 0; index++) {
            if ((buf.peekUnsignedByte(0) == 0) && (buf.peekUnsignedByte(1) == 0)) {
                break;
            }

            Tag tag = this.type.deepCopy(index);

            tag.readTag(buf);
            tagList.add(tag);
        }

        this.tags = ((Tag[]) tagList.toArray(new Tag[tagList.size()]));
    }

    public Tag deepCopy(String suffix) {
        return new SequenceOf(this.name + suffix).copyFrom(this);
    }

    public Tag copyFrom(Tag tag) {
        super.copyFrom(tag);

        this.type = ((SequenceOf) tag).type;

        this.tags = new Tag[((Sequence) tag).tags.length];
        for (int i = 0; i < this.tags.length; i++) {
            this.tags[i] = ((Sequence) tag).tags[i].deepCopy("");
        }

        return this;
    }

    public String toString() {
        return super.toString() + ": " + this.type;
    }
}
