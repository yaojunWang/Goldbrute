package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.OctetString;
import rdp.gold.brute.rdp.Messages.common.asn1.Sequence;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class TSPasswordCreds extends Sequence {
    public OctetString domainName = new OctetString("domainName") {
    };

    public OctetString userName = new OctetString("userName") {
    };

    public OctetString password = new OctetString("password") {
    };

    public TSPasswordCreds(String name) {
        super(name);
        this.tags = new Tag[] { this.domainName, this.userName, this.password };
    }

    public Tag deepCopy(String suffix) {
        return new TSPasswordCreds(this.name + suffix).copyFrom(this);
    }
}
