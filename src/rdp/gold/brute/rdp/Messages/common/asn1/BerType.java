package rdp.gold.brute.rdp.Messages.common.asn1;

public class BerType {
    public int tagClass;

    public boolean constructed;

    public int typeOrTagNumber;

    public BerType() {
    }

    public BerType(int tagClass, boolean constructed, int typeOrTagNumber) {
        this.tagClass = tagClass;
        this.constructed = constructed;
        this.typeOrTagNumber = typeOrTagNumber;
    }

    public String toString() {
        return "BerType [tagClass=" + Tag.tagClassToString(this.tagClass) + ", constructed=" + this.constructed + ", type or tag number="
                + Tag.tagTypeOrNumberToString(this.tagClass, this.typeOrTagNumber) + "]";
    }
}
