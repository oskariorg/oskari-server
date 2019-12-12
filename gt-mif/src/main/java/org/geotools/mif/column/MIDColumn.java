package org.geotools.mif.column;

import org.geotools.mif.MIFFeatureReader;

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
        String name = column.substring(0, i);
        if (MIFFeatureReader.GEOM_COLUMN_NAME.equals(name)) {
            throw new IllegalArgumentException(
                    "Name of a data column can not be " + MIFFeatureReader.GEOM_COLUMN_NAME);
        }
        String type = column.substring(i + 1).toLowerCase();
        return createByType(name, type);
    }

    private static MIDColumn createByType(String name, String type) {
        if (type.startsWith("char")) {
            return new CharMIDColumn(name);
        }
        if (type.startsWith("integer")) {
            return new IntegerMIDColumn(name);
        }
        if (type.startsWith("smallint")) {
            return new SmallIntMIDColumn(name);
        }
        if (type.startsWith("decimal")) {
            int i = type.indexOf('(');
            int j = type.indexOf(',', i + 1);
            int k = type.indexOf(')', j + 1);
            int numDecimals = Integer.parseInt(type.substring(j + 1, k));
            return numDecimals == 0
                    ? new LongMIDColumn(name)
                    : new DoubleMIDColumn(name);
        }
        if (type.startsWith("float")) {
            return new FloatMIDColumn(name);
        }
        if (type.startsWith("date")) {
            return new DateMIDColumn(name);
        }
        if (type.startsWith("logical")) {
            return new LogicalMIDColumn(name);
        }
        throw new IllegalArgumentException("Invalid type");
    }

    public abstract Object parse(String str) throws IllegalArgumentException;
    public abstract Class<?> getAttributeClass();

}
