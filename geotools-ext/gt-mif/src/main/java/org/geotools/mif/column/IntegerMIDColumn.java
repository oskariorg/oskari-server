package org.geotools.mif.column;

public class IntegerMIDColumn extends MIDColumn {

    public IntegerMIDColumn(String name) {
        super(name);
    }

    @Override
    public Integer parse(String str) {
        return Integer.parseInt(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return Integer.class;
    }

}
