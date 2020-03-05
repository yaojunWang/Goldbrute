package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.BitString;
import rdp.gold.brute.rdp.Messages.common.asn1.Sequence;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class SubjectPublicKeyInfo extends Sequence {
    public AlgorithmIdentifier algorithm = new AlgorithmIdentifier("algorithm");
    public BitString subjectPublicKey = new BitString("subjectPublicKey");

    public SubjectPublicKeyInfo(String name) {
        super(name);
        this.tags = new Tag[] { this.algorithm, this.subjectPublicKey };
    }
}
