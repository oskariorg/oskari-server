package org.geotools.mif.column;

public class SmallIntMIDColumn extends MIDColumn {

    public SmallIntMIDColumn(String name) {
        super(name);
    }

    @Override
    public Short parse(String str) {
        return Short.parseShort(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return Short.class;
    }

}
