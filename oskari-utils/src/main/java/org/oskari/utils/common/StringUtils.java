package org.oskari.utils.common;

import java.util.Optional;

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

    public static Optional<double[]> parseDoubleArray(final String str, final char c) {
        final int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return Optional.empty();
        }

        try {
            double[] arr = new double[16];
            int n = 0;
            int i = 0;
            while (i < strLen) {
                int j = str.indexOf(c, i);
                if (j < 0) {
                    break;
                }
                if (n == arr.length) {
                    int len = arr.length;
                    double[] tmp = new double[len * 2];
                    System.arraycopy(arr, 0, tmp, 0, len);
                    arr = tmp;
                }
                arr[n++] = Double.parseDouble(str.substring(i, j));
                i = j + 1;
            }
            double[] tmp = new double[n + 1];
            System.arraycopy(arr, 0, tmp, 0, n);
            tmp[n] = Double.parseDouble(str.substring(i));
            return Optional.of(tmp);
        } catch (NumberFormatException ignore) {
            return Optional.empty();
        }
    }

    public static Optional<int[]> parseIntArray(String str, char c) {
        final int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return Optional.empty();
        }

        try {
            int[] arr = new int[16];
            int n = 0;
            int i = 0;
            while (i < strLen) {
                int j = str.indexOf(c, i);
                if (j < 0) {
                    break;
                }
                if (n == arr.length) {
                    int len = arr.length;
                    int[] tmp = new int[len * 2];
                    System.arraycopy(arr, 0, tmp, 0, len);
                    arr = tmp;
                }
                arr[n++] = Integer.parseInt(str.substring(i, j));
                i = j + 1;
            }
            int[] tmp = new int[n + 1];
            System.arraycopy(arr, 0, tmp, 0, n);
            tmp[n] = Integer.parseInt(str.substring(i));
            return Optional.of(tmp);
        } catch (NumberFormatException ignore) {
            return Optional.empty();
        }
    }

}
