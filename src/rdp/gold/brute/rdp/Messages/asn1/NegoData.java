package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.SequenceOf;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class NegoData extends SequenceOf {
    public NegoData(String name) {
        super(name);
        this.type = new NegoItem("NegoItem");
    }

    public Tag deepCopy(String suffix) {
        return new NegoData(this.name + suffix).copyFrom(this);
    }
}
