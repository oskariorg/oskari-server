package org.geotools.mif.column;

/**
 * This is actually a DecimalMIDColumn, but with non-zero decimals
 */
public class DoubleMIDColumn extends MIDColumn {

    public DoubleMIDColumn(String name) {
        super(name);
    }

    @Override
    public Double parse(String str) {
        if (str.isEmpty()) {
            return null;
        }
        return Double.parseDouble(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return Double.class;
    }

}
