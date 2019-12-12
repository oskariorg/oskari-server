package org.geotools.mif.util;

/**
 * If delimiter character is included as part of the data in a field, enclose the field in
 * quotation marks.
 */
public class FieldSplitter {

    private final String str;
    private final char delimiter;
    private int off;

    public FieldSplitter(String str, char delimiter) {
        this.str = str;
        this.delimiter = delimiter;
        this.off = 0;
    }

    public String next() {
        int i = off;
        int j;
        char ch = str.charAt(i);
        if (ch == '"') {
            i++;
            j = str.indexOf('"', i);
        } else {
            j = str.indexOf(delimiter, i);
        }
        String ret;
        if (j < 0) {
            ret = str.substring(off);
            off = str.length();
        } else {
            ret = str.substring(off, i);
            off = i + 1;
        }
        return ret;
    }

}
