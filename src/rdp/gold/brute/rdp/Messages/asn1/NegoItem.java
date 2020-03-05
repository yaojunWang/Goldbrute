package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.OctetString;
import rdp.gold.brute.rdp.Messages.common.asn1.Sequence;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class NegoItem extends Sequence {
    public OctetString negoToken = new OctetString("negoToken") {
    };

    public NegoItem(String name) {
        super(name);
        this.tags = new Tag[] { this.negoToken };
    }

    public Tag deepCopy(String suffix) {
        return new NegoItem(this.name + suffix).copyFrom(this);
    }
}
