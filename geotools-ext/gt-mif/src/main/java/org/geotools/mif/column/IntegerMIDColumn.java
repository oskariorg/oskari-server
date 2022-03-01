package org.geotools.mif.column;

public class IntegerMIDColumn extends MIDColumn {

    public IntegerMIDColumn(String name) {
        super(name);
    }

    @Override
    public Integer parse(String str) {
        if (str.isEmpty()) {
            return null;
        }
        return Integer.parseInt(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return Integer.class;
    }

}
