package rdp.gold.brute.rdp.Messages.common.asn1;

public abstract interface Asn1Constants {
    public static final int UNIVERSAL_CLASS = 0;
    public static final int APPLICATION_CLASS = 64;
    public static final int CONTEXT_CLASS = 128;
    public static final int PRIVATE_CLASS = 192;
    public static final int CONSTRUCTED = 32;
    public static final int CLASS_MASK = 192;
    public static final int TYPE_MASK = 31;
    public static final int EOF = 0;
    public static final int BOOLEAN = 1;
    public static final int INTEGER = 2;
    public static final int BIT_STRING = 3;
    public static final int OCTET_STRING = 4;
    public static final int NULL = 5;
    public static final int OBJECT_ID = 6;
    public static final int REAL = 9;
    public static final int ENUMERATED = 10;
    public static final int SEQUENCE = 16;
    public static final int SET = 17;
    public static final int NUMERIC_STRING = 18;
    public static final int PRINTABLE_STRING = 19;
    public static final int TELETEX_STRING = 20;
    public static final int VIDEOTEXT_STRING = 21;
    public static final int IA5_STRING = 22;
    public static final int UTCTIME = 23;
    public static final int GENERAL_TIME = 24;
    public static final int GRAPHIC_STRING = 25;
    public static final int VISIBLE_STRING = 26;
    public static final int GENERAL_STRING = 27;
    public static final int EXTENDED_TYPE = 31;
}
