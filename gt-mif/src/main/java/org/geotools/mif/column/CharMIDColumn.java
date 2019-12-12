package org.geotools.mif.column;

public class CharMIDColumn extends MIDColumn {

    public CharMIDColumn(String name) {
        super(name);
    }

    @Override
    public String parse(String str) {
        // Replace escaped "\n" with actual line feeds
        return str.replace("\\n", "\n");
    }

    @Override
    public Class<?> getAttributeClass() {
        return String.class;
    }

}
