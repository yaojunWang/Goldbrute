package rdp.gold.brute.rdp.Messages.asn1;

import rdp.gold.brute.rdp.Messages.common.asn1.Any;
import rdp.gold.brute.rdp.Messages.common.asn1.ObjectID;
import rdp.gold.brute.rdp.Messages.common.asn1.Sequence;
import rdp.gold.brute.rdp.Messages.common.asn1.Tag;

public class AlgorithmIdentifier extends Sequence {
    public ObjectID algorithm = new ObjectID("algorithm");
    public Any parameters = new Any("parameters") {
    };

    public AlgorithmIdentifier(String name) {
        super(name);
        this.tags = new Tag[] { this.algorithm, this.parameters };
    }
}
