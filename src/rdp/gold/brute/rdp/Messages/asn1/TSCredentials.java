package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.Asn1Integer;
import rdp.gold.brute.rdp.Messages.common.asn1.OctetString;
import rdp.gold.brute.rdp.Messages.common.asn1.Sequence;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class TSCredentials extends Sequence {
    public Asn1Integer credType = new Asn1Integer("credType") {
    };

    public OctetString credentials = new OctetString("credentials") {
    };

    public TSCredentials(String name) {
        super(name);
        this.tags = new Tag[] { this.credType, this.credentials };
    }

    public Tag deepCopy(String suffix) {
        return new TSCredentials(this.name + suffix).copyFrom(this);
    }
}
