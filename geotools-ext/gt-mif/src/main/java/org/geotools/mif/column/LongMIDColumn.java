package org.geotools.mif.column;

/**
 * This is actually a DecimalMIDColumn, but with 0 decimals
 */
public class LongMIDColumn extends MIDColumn {

    public LongMIDColumn(String name) {
        super(name);
    }

    @Override
    public Long parse(String str) {
        return Long.parseLong(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return Long.class;
    }

}
