package fi.nls.oskari.search.util;

import java.util.Arrays;

/**
 * EUREF-FIN (TM35) Map Sheet Division / Karttalehtijako
 * @see http://www.jhs-suositukset.fi/suomi/jhs197
 * @see http://docs.jhs-suositukset.fi/jhs-suositukset/JHS197_liite8/JHS197_liite8.pdf
 * @see http://docs.jhs-suositukset.fi/jhs-suositukset/JHS197_liite9/JHS197_liite9.pdf
 */
public class TM35MapSheetDivision {

    private static final int[] SCALE_DENOMINATORS = {
            200000, 100000, 50000, 25000, 10000, 5000
    };

    private static final int CENTRAL_MERIDIAN = 500000;

    private static final int MIN_EAST = 20000;
    private static final int MAX_EAST = 788000;
    private static final int MIN_NORTH = 6570000;
    private static final int MAX_NORTH = 7818000;

    private static final int MK200K_W = 192000;
    private static final int MK200K_H = 96000;
    private static final int MK10K_SIZE = 6000;
    private static final int MK10K_STR_LEN = 6;
    private static final int MK5K_STR_LEN = 7;
    private static final int MIN_LEN = 2;
    private static final int MAX_LEN = MK5K_STR_LEN + 1; // Allow L(eft) and R(ight) extra char

    /**
     * Get the extent of a map sheet in EPSG:3067
     * Doesn't check for the validity of the @param mapSheet
     * @return [eastMin,northMin,eastMax,northMax]
     */
    public static int[] getBoundingBox(String mapSheet)
            throws IllegalArgumentException {
        int len = mapSheet.length();
        if (len < MIN_LEN || len > MAX_LEN) {
            throw new IllegalArgumentException("Invalid length");
        }

        // Check if last character is either 'L'(eft) or 'R'(ight)
        char lastChar = mapSheet.charAt(len - 1);
        if (lastChar == 'L' || lastChar == 'R') {
            len--;
        }

        int width = MK200K_W;
        int height = MK200K_H;

        char c = mapSheet.charAt(0);
        // Letter 'O' is skipped to avoid confusion with zero
        if (c > 'O') {
            c--;
        }

        // K5 bottom left is at (500000, 6570000) == (CENTRAL_MERIDIAN, MIN_NORTH)
        int yMin = MIN_NORTH + (c - 'K') * height;
        int xMin = CENTRAL_MERIDIAN + (mapSheet.charAt(1) - '5') * width;

        // MK100K-MK25K
        for (int i = 2; i < Math.min(MK10K_STR_LEN - 1, len); i++) {
            width = height;
            height /= 2;
            int j = mapSheet.charAt(i) - '1';
            if (j > 1) {
                xMin += width;
            }
            if ((j % 2) != 0) {
                yMin += height;
            }
        }

        if (len >= MK10K_STR_LEN) {
            width = MK10K_SIZE;
            height = width;
            int j = mapSheet.charAt(MK10K_STR_LEN - 1) - 'A';
            int k = j / 2;
            if (k > 0) {
                xMin += k * width;
            }
            if ((j % 2) != 0) {
                yMin += height;
            }
        }

        if (len >= MK5K_STR_LEN) {
            width /= 2;
            height = width;
            int j = mapSheet.charAt(MK5K_STR_LEN - 1) - '1';
            if (j > 1) {
                xMin += width;
            }
            if ((j % 2) != 0) {
                yMin += height;
            }
        }

        // If last character was R then move east half a width to the right
        // (If it was L then we don't have to do anything)
        if (lastChar == 'R') {
            width /= 2;
            xMin += width;
        }

        return new int[] { xMin, yMin, xMin + width, yMin + height };
    }

    /**
     * Get the name of the map sheet a coordinate belongs to
     * If the coordinate is on the border we always choose the sheet
     * on the right side and/or on the top side of the border
     *
     * @param e east coordinate in EPSG:3067
     * @param n north coordinate in EPSG:3067
     * @param len number of characters you want,
     *      @see {@link #getLen(int scaleDenominator)}
     * @param leftRight true if you want the sheet suffixed by L(eft)/R(ight)
     * @return name of the map sheet as a String
     */
    public static String getMapSheetByCoordinate(double e, double n, int len, boolean leftRight)
            throws IllegalArgumentException {
        if (len < MIN_LEN || len > MAX_LEN) {
            throw new IllegalArgumentException("Invalid length");
        }
        if (!validate(e, n)) {
            throw new IllegalArgumentException("Coordinate outside of map sheet division");
        }

        char[] arr = new char[len];

        if (leftRight) {
            len--;
        }

        int width = MK200K_W;
        int height = MK200K_H;

        int yOff = (int) (n - MIN_NORTH);
        int tmp = yOff / height;
        char c = (char) ('K' + tmp);
        // Letter 'O' is skipped to avoid confusion with zero
        if (c >= 'O') {
            c++;
        }
        arr[0] = c;
        yOff -= tmp * height;

        int xOff = (int) (e - CENTRAL_MERIDIAN);
        tmp = xOff / width;
        if (e < CENTRAL_MERIDIAN) {
            tmp--;
        }
        arr[1] = (char) (tmp + '5');

        xOff -= tmp * width;

        for (int i = 2; i < Math.min(MK10K_STR_LEN - 1, len); i++) {
            width = height;
            height /= 2;
            if (xOff < width) {
                tmp = 1;
            } else {
                // x is on the right side of the tile
                // xOff goes left
                tmp = 3;
                xOff -= width;
            }
            if (yOff >= height) {
                // x is on the top side of the tile
                // yOff goes down
                // tmp++ does 1 => 2 or 3 => 4
                tmp++;
                yOff -= height;
            }
            arr[i] = (char) (tmp + '0');
        }

        if (len >= MK10K_STR_LEN) {
            width = MK10K_SIZE;
            height = width;
            tmp = xOff / width;
            xOff -= tmp * width;
            tmp *= 2;
            if (yOff > height) {
                tmp++;
                yOff -= height;
            }
            arr[5] = (char) (tmp + 'A');
        }

        if (len >= MK5K_STR_LEN) {
            width /= 2;
            height = width;
            tmp = xOff < width ? 1 : 3;
            if (yOff >= height) {
                tmp++;
            }
            arr[6] = (char) (tmp + '0');
        }

        if (leftRight) {
            arr[len - 1] = xOff < (width / 2) ? 'L' : 'R';
        }

        return new String(arr);
    }

    public static boolean validate(String mapSheet) {
        int len = mapSheet.length();
        if (len < MIN_LEN || len > MAX_LEN) {
            return false;
        }

        char lastChar = mapSheet.charAt(len - 1);
        if (lastChar == 'L' || lastChar == 'R') {
            len--;
        }

        char c = mapSheet.charAt(0);
        if (c < 'K' || c == 'O' || c > 'X') {
            return false;
        }

        c = mapSheet.charAt(1);
        if (c < '2' || c > '6') {
            return false;
        }

        for (int i = 2; i < Math.min(MK10K_STR_LEN - 1, len); i++) {
            c = mapSheet.charAt(i);
            if (c < '1' || c > '4') {
                return false;
            }
        }

        if (len >= MK10K_STR_LEN) {
            c = mapSheet.charAt(MK10K_STR_LEN - 1);
            if (c < 'A' || c > 'H') {
                return false;
            }
        }

        if (len >= MK5K_STR_LEN) {
            c = mapSheet.charAt(MK5K_STR_LEN - 1);
            if (c < '1' || c > '4') {
                return false;
            }
        }

        return true;
    }

    public static boolean validate(double e, double n) {
        if (e < MIN_EAST || e >= MAX_EAST) {
            return false;
        }
        if (n < MIN_NORTH || n >= MAX_NORTH) {
            return false;
        }
        if (e < 116000) {
            if (n >= 6762000) { // Only allow L2,K2
                return false;
            }
        } else if (e < 308000) {
            if (n >= 7722000 // West of X4
                    || (n >= 7146000 && n < 7530000)) { // West of R4 - U4
                return false;
            }
        } else if (e >= 692000) {
            // Only allow N6 and P6
            if (n < 6858000 || n >= 7050000) {
                return false;
            }
        } else if (e >= CENTRAL_MERIDIAN) {
            if (n < 6666000) {
                return false; // East of K4
            }
        }
        return true;
    }

    public static int getScaleDenominator(String mapSheet) {
        return getScaleDenominator(mapSheet.length());
    }

    public static int getScaleDenominator(int numberOfChars) {
        if (numberOfChars < MIN_LEN || numberOfChars > MAX_LEN) {
            throw new IllegalArgumentException("Invalid number of characters");
        }
        return SCALE_DENOMINATORS[numberOfChars - 2];
    }

    public static int getLen(int scaleDenominator) {
        for (int i = 0; i < SCALE_DENOMINATORS.length; i++) {
            int tmp = SCALE_DENOMINATORS[i];
            if (scaleDenominator == tmp) {
                return i + 2;
            }
            if (scaleDenominator > tmp) {
                break;
            }
        }
        throw new IllegalArgumentException("Scale denominator not one of "
                + Arrays.toString(SCALE_DENOMINATORS));
    }

}
