package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.Asn1Integer;
import rdp.gold.brute.rdp.Messages.common.asn1.OctetString;
import rdp.gold.brute.rdp.Messages.common.asn1.Sequence;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class TSCspDataDetail extends Sequence {
    public Asn1Integer keySpec = new Asn1Integer("keySpec") {
    };

    public OctetString cardName = new OctetString("cardName") {
    };

    public OctetString readerName = new OctetString("readerName") {
    };

    public OctetString containerName = new OctetString("containerName") {
    };

    public OctetString cspName = new OctetString("cspName") {
    };

    public TSCspDataDetail(String name) {
        super(name);
        this.tags = new Tag[] { this.keySpec, this.cardName, this.readerName, this.containerName, this.cspName };
    }

    public Tag deepCopy(String suffix) {
        return new TSCspDataDetail(this.name + suffix).copyFrom(this);
    }
}
