package org.geotools.mif.column;

public class FloatMIDColumn extends MIDColumn {

    public FloatMIDColumn(String name) {
        super(name);
    }

    @Override
    public Float parse(String str) {
        return Float.parseFloat(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return Float.class;
    }

}
