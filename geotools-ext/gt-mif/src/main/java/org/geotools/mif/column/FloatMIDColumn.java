package org.geotools.mif.column;

public class FloatMIDColumn extends MIDColumn {

    public FloatMIDColumn(String name) {
        super(name);
    }

    @Override
    public Float parse(String str) {
        if (str.isEmpty()) {
            return null;
        }
        return Float.parseFloat(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return Float.class;
    }

}
