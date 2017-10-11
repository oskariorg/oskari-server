package org.oskari.utils.common;

public class StringUtils {

    public static String join(String[] a, char c) {
        if (a == null) {
            return null;
        }
        if (a.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(a[0]);
        for (int i = 1; i < a.length; i++) {
            sb.append(c);
            sb.append(a[i]);
        }
        return sb.toString();
    }

}
