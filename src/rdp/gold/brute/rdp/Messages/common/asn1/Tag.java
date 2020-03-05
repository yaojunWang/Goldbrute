package rdp.gold.brute.rdp.Messages.common.asn1;

import rdp.gold.brute.rdp.ByteBuffer;

public abstract class Tag implements Asn1Constants {
    public String name = "";

    public boolean optional = false;

    public boolean constructed = false;

    public int tagClass = 0;

    public int tagNumber = -1;

    public int tagType = -1;

    public boolean explicit = false;

    public Tag(String name) {
        this.name = name;
    }

    public static final String tagTypeOrNumberToString(int tagClass, int tagTypeOrNumber) {
        switch (tagClass) {
        case 0:
            switch (tagTypeOrNumber) {
            case 0:
                return "EOF";
            case 1:
                return "BOOLEAN";
            case 2:
                return "INTEGER";
            case 3:
                return "BIT_STRING";
            case 4:
                return "OCTET_STRING";
            case 5:
                return "NULL";
            case 6:
                return "OBJECT_ID";
            case 9:
                return "REAL";
            case 10:
                return "ENUMERATED";
            case 16:
                return "SEQUENCE";
            case 17:
                return "SET";
            case 18:
                return "NUMERIC_STRING";
            case 19:
                return "PRINTABLE_STRING";
            case 20:
                return "TELETEX_STRING";
            case 21:
                return "VIDEOTEXT_STRING";
            case 22:
                return "IA5_STRING";
            case 23:
                return "UTCTIME";
            case 24:
                return "GENERAL_TIME";
            case 25:
                return "GRAPHIC_STRING";
            case 26:
                return "VISIBLE_STRING";
            case 27:
                return "GENERAL_STRING";
            case 31:
                return "EXTENDED_TYPE (multibyte)";
            }
            return "UNKNOWN(" + tagTypeOrNumber + ")";
        }

        return "[" + tagTypeOrNumber + "]";
    }

    public static final String tagClassToString(int tagClass) {
        switch (tagClass) {
        case 0:
            return "UNIVERSAL";
        case 128:
            return "CONTEXT";
        case 64:
            return "APPLICATION";
        case 192:
            return "PRIVATE";
        }
        return "UNKNOWN";
    }

    public void writeTag(ByteBuffer buf) {
        if (!isMustBeWritten()) {
            return;
        }

        if (this.explicit) {

            BerType berTagPrefix = new BerType(this.tagClass, true, this.tagNumber);
            writeBerType(buf, berTagPrefix);

            buf.writeBerLength(calculateLength());

            writeTagValue(buf);
        } else {
            writeTagValue(buf);
        }
    }

    public boolean isMustBeWritten() {
        return (!this.optional) || (isValueSet());
    }

    public abstract boolean isValueSet();

    public long calculateFullLength() {
        if (!isMustBeWritten()) {
            return 0L;
        }

        long length = calculateLength();

        if (!this.explicit) {
            length += calculateLengthOfTagTypeOrTagNumber(this.tagType) + calculateLengthOfLength(length);
        } else {
            length += calculateLengthOfTagTypeOrTagNumber(this.tagNumber) + calculateLengthOfLength(length);
        }

        return length;
    }

    public long calculateLength() {
        if (!isMustBeWritten()) {
            return 0L;
        }

        long length = calculateLengthOfValuePayload();

        if (this.explicit) {
            length += calculateLengthOfTagTypeOrTagNumber(this.tagType) + calculateLengthOfLength(length);
        }

        return length;
    }

    public int calculateLengthOfLength(long length) {
        if (length < 0L) {
            throw new RuntimeException("[" + this + "] ERROR: Length of tag cannot be less than zero: " + length + ".");
        }
        if (length <= 127L)
            return 1;
        if (length <= 255L)
            return 2;
        if (length <= 65535L)
            return 3;
        if (length <= 16777215L)
            return 4;
        if (length <= 4294967295L)
            return 5;
        if (length <= 1099511627775L)
            return 6;
        if (length <= 281474976710655L)
            return 7;
        if (length <= 72057594037927935L) {
            return 8;
        }
        return 9;
    }

    public int calculateLengthOfTagTypeOrTagNumber(int tagType) {
        if (tagType >= 31) {
            throw new RuntimeException("Multibyte tag types are not supported yet.");
        }
        return 1;
    }

    public abstract long calculateLengthOfValuePayload();

    public void writeTagValue(ByteBuffer buf) {
        BerType valueType = new BerType(0, this.constructed, this.tagType);
        writeBerType(buf, valueType);

        long lengthOfPayload = calculateLengthOfValuePayload();
        buf.writeBerLength(lengthOfPayload);

        int storedCursor = buf.cursor;

        writeTagValuePayload(buf);

        int actualLength = buf.cursor - storedCursor;
        if (actualLength != lengthOfPayload) {
            throw new RuntimeException("[" + this + "] ERROR: Unexpected length of data in buffer. Expected " + lengthOfPayload + " of bytes of payload, but " + actualLength
                    + " bytes are written instead. Data: " + buf + ".");
        }
    }

    public abstract void writeTagValuePayload(ByteBuffer paramByteBuffer);

    public void readTag(ByteBuffer buf) {
        BerType typeAndFlags = readBerType(buf);

        if (!isTypeValid(typeAndFlags)) {
            throw new RuntimeException("[" + this + "] Unexpected type: " + typeAndFlags + ".");
        }
        readTag(buf, typeAndFlags);
    }

    public void readTag(ByteBuffer buf, BerType typeAndFlags) {
        if (this.explicit) {
            long length = buf.readBerLength();

            if (length > buf.length) {
                throw new RuntimeException("BER value is too long: " + length + " bytes. Data: " + buf + ".");
            }
            ByteBuffer value = buf.readBytes((int) length);

            readTagValue(value);

            value.unref();
        } else {
            readTagValue(buf, typeAndFlags);
        }
    }

    public void readTagValue(ByteBuffer value) {
        BerType typeAndFlags = readBerType(value);

        if (!isTypeValid(typeAndFlags, false)) {
            throw new RuntimeException("[" + this + "] Unexpected type: " + typeAndFlags + ".");
        }
        readTagValue(value, typeAndFlags);
    }

    public final boolean isTypeValid(BerType typeAndFlags) {
        return isTypeValid(typeAndFlags, this.explicit);
    }

    public boolean isTypeValid(BerType typeAndFlags, boolean explicit) {
        if (explicit) {
            return (typeAndFlags.tagClass == this.tagClass) && (typeAndFlags.constructed) && (typeAndFlags.typeOrTagNumber == this.tagNumber);
        }
        return (typeAndFlags.tagClass == 0) && (!typeAndFlags.constructed) && (typeAndFlags.typeOrTagNumber == this.tagType);
    }

    public String toString() {
        return

        "  \nTag [name=" + this.name + (this.constructed ? ", constructed=" + this.constructed : "") + ", tagType=" + tagTypeOrNumberToString(0, this.tagType)
                + (this.explicit
                        ? ", explicit=" + this.explicit + ", optional=" + this.optional + ", tagClass=" + tagClassToString(this.tagClass) + ", tagNumber="
                                + tagTypeOrNumberToString(this.tagClass, this.tagNumber)
                        : "")
                + "]";
    }

    public BerType readBerType(ByteBuffer buf) {
        int typeAndFlags = buf.readUnsignedByte();

        int tagClass = typeAndFlags & 0xC0;

        boolean constructed = (typeAndFlags & 0x20) != 0;

        int type = typeAndFlags & 0x1F;
        if (type == 31) {
            throw new RuntimeException("Extended tag types/numbers (31+) are not supported yet.");
        }
        return new BerType(tagClass, constructed, type);
    }

    public void writeBerType(ByteBuffer buf, BerType berType) {
        if ((berType.typeOrTagNumber >= 31) || (berType.typeOrTagNumber < 0)) {
            throw new RuntimeException("Extended tag types/numbers (31+) are not supported yet: " + berType + ".");
        }
        if ((berType.tagClass & 0xC0) != berType.tagClass) {
            throw new RuntimeException("Value of BER tag class is out of range: " + berType.tagClass + ". Expected values: " + 0 + ", " + 128 + ", " + 64 + ", " + 192 + ".");
        }

        int typeAndFlags = berType.tagClass | (berType.constructed ? 32 : 0) | berType.typeOrTagNumber;

        buf.writeByte(typeAndFlags);
    }

    public abstract void readTagValue(ByteBuffer paramByteBuffer, BerType paramBerType);

    public abstract Tag deepCopy(String paramString);

    public Tag deepCopy(int index) {
        return deepCopy("[" + index + "]");
    }

    public Tag copyFrom(Tag tag) {
        this.constructed = tag.constructed;
        this.explicit = tag.explicit;
        this.optional = tag.optional;
        this.tagClass = tag.tagClass;
        this.tagNumber = tag.tagNumber;
        return this;
    }
}
