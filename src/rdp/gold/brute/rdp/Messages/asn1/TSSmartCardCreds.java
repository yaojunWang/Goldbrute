package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.OctetString;
import rdp.gold.brute.rdp.Messages.common.asn1.Sequence;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class TSSmartCardCreds extends Sequence {
    public OctetString pin = new OctetString("pin") {
    };

    public TSCspDataDetail cspData = new TSCspDataDetail("cspData") {
    };

    public OctetString userHint = new OctetString("userHint") {
    };

    public OctetString domainHint = new OctetString("domainHint") {
    };

    public TSSmartCardCreds(String name) {
        super(name);
        this.tags = new Tag[] { this.pin, this.cspData, this.userHint, this.domainHint };
    }

    public Tag deepCopy(String suffix) {
        return new TSSmartCardCreds(this.name + suffix).copyFrom(this);
    }
}
