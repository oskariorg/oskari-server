package org.geotools.mif.util;

/**
 * If delimiter character is included as part of the data in a field, enclose the field in
 * quotation marks.
 */
public class MIDFieldSplitter {

    private final String str;
    private final char delimiter;
    private int off;
    private boolean end;

    public MIDFieldSplitter(String str, char delimiter) {
        this.str = str;
        this.delimiter = delimiter;
        this.off = -1;
        this.end = false;
    }

    public String next() {
        if (end) {
            return null;
        }
        int i = off + 1;
        if (i >= str.length()) {
            end = true;
            return "";
        }
        int j;
        if (str.charAt(i) == '"') {
            i += 1;
            j = str.indexOf('"', i);
            if (j < 0) {
                throw new IllegalArgumentException("Could not find closing quote");
            }
            off = str.indexOf(delimiter, j + 1);
            if (off < 0) {
                end = true;
            }
        } else {
            j = str.indexOf(delimiter, i);
            if (j < 0) {
                j = str.length();
                end = true;
            } else {
                off = j;
            }
        }
        return str.substring(i, j);
    }

}
