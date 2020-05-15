package org.geotools.mif.column;

import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.mif.util.MIFUtil;

public abstract class MIDColumn {

    private final String name;

    public MIDColumn(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MIDColumn create(String column) {
        column = column.trim();
        int i = column.indexOf(' ');
        if (i < 0) {
            i = column.indexOf('\t');
        }
        String name = column.substring(0, i);
        String type = column.substring(i + 1);
        if (BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME.equals(name)) {
            throw new IllegalArgumentException(
                    "Name of a data column can not be " + BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME);
        }
        return createByType(name, type);
    }

    private static MIDColumn createByType(String name, String type) {
        if (MIFUtil.startsWithIgnoreCase(type, "char")) {
            return new CharMIDColumn(name);
        } else if (MIFUtil.startsWithIgnoreCase(type, "integer")) {
            return new IntegerMIDColumn(name);
        } else if (MIFUtil.startsWithIgnoreCase(type, "smallint")) {
            return new SmallIntMIDColumn(name);
        } else if (MIFUtil.startsWithIgnoreCase(type, "decimal")) {
            int i = type.indexOf('(');
            int j = type.indexOf(',', i + 1);
            int k = type.indexOf(')', j + 1);
            int numDecimals = Integer.parseInt(type.substring(j + 1, k).trim());
            return numDecimals == 0
                    ? new LongMIDColumn(name)
                    : new DoubleMIDColumn(name);
        } else if (MIFUtil.startsWithIgnoreCase(type, "float")) {
            return new FloatMIDColumn(name);
        } else if (MIFUtil.startsWithIgnoreCase(type, "date")) {
            return new DateMIDColumn(name);
        } else if (MIFUtil.startsWithIgnoreCase(type, "logical")) {
            return new LogicalMIDColumn(name);
        } else {
            throw new IllegalArgumentException("Invalid type");
        }
    }

    public abstract Object parse(String str) throws IllegalArgumentException;
    public abstract Class<?> getAttributeClass();

}
