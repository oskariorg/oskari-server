package org.geotools.mif.column;

import java.time.LocalDate;

public class DateMIDColumn extends MIDColumn {

    public DateMIDColumn(String name) {
        super(name);
    }

    @Override
    public LocalDate parse(String str) {
        if (str.isEmpty()) {
            return null;
        }
        return LocalDate.parse(str);
    }

    @Override
    public Class<?> getAttributeClass() {
        return LocalDate.class;
    }

}
