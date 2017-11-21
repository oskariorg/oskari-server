package fi.nls.oskari.util;

import java.util.*;

/**
 * Conversion helper methods
 */
public class ConversionHelper {

    /**
     * Returns the first of the given parameters that is not null or null if all are null
     * @param strings
     * @return
     */
    public static String firstNonNull(final String ... strings) {
        for (final String s : strings) {
            if (null != s) {
                return s;
            }
        }
        return null;
    }

    /**
     * Count the number of instances of substring within a string.
     *
     * @param string     String to look for substring in.
     * @param substring  Sub-string to look for.
     * @return           Count of substrings in string.
     */
    public static int count(final String string, final String substring)
    {
        int count = 0;
        int idx = 0;

        while ((idx = string.indexOf(substring, idx)) != -1)
        {
            idx++;
            count++;
        }

        return count;
    }


    /**
     * Makes the first letter of param to be lowercase while maintaining other parts as is.
     * @param param
     * @param allCapsStringToLowercase true to turn HTTP -> http, false to turn HTTP -> hTTP
     * @return null fo null, empty string for empty string, otherwise lowercase startLetter
     */
    public static String decapitalize(String param, boolean allCapsStringToLowercase) {
        if(param == null || param.isEmpty()) {
            return param;
        }
        if(allCapsStringToLowercase && isOnlyUpperCase(param)) {
            return param.toLowerCase();
        }
        final char c = Character.toLowerCase(param.charAt(0));
        if(param.length() == 1) {
            return "" + c;
        }
        return c + param.substring(1);

    }

    /**
     * Makes the first letter of param to be lowercase while maintaining other parts as is.
     * Converts param with only uppercase characters to lowercase as a whole.
     * @param param
     * @return Returns:
     * - null for null
     * - empty string for empty string
     * - param with only uppercase LETTERS to lowercase letters
     * - otherwise lowercase startLetter
     */
    public static String decapitalize(final String param) {
        return decapitalize(param, true);
    }

    /**
     * Checks parameter for lowercase letters and returns false if lowercase letters found or param was null.
     * @param param
     * @return true if param has only uppercase LETTERS
     */
    public static boolean isOnlyUpperCase(final String param) {
        if(param == null) {
            return false;
        }
        for (char c : param.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
    /**
     * Returns a string that if its not null and default value if it is
     *
     * @param str
     * @param defaultValue
     * @return string
     */
    public static final String getString(final String str, final String defaultValue) {
        if (str != null) {
            return str;
        }
        return defaultValue;
    }

    /**
     * Parses long from String
     *
     * @param strToParse
     * @param defaultValue
     * @return long
     */
    public static final long getLong(final String strToParse, final long defaultValue) {
        if (strToParse == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(strToParse);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses int from String
     *
     * @param strToParse
     * @param defaultValue
     * @return
     */
    public static final int getInt(final String strToParse, final int defaultValue) {
        if(strToParse == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(strToParse);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses double from String
     *
     * @param strToParse
     * @param defaultValue
     * @return
     */
    public static final double getDouble(final String strToParse, final double defaultValue) {
        try {
            return Double.parseDouble(strToParse);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses boolean from String. Returns defaultValue if strToParse is null.
     *
     * @param strToParse
     * @param defaultValue
     * @return
     */
    public static final boolean getBoolean(final String strToParse, final boolean defaultValue) {
        if(strToParse == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(strToParse);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Parses boolean from on/off String. Returns defaultValue if strToParse is null.
     *
     * @param strToParse
     * @param defaultValue
     * @return
     */
    public static final boolean getOnOffBoolean(final String strToParse, final boolean defaultValue) {
        if(strToParse == null) {
            return defaultValue;
        }
        try {
            return "ON".equalsIgnoreCase(strToParse);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Pickups real string values out of String List - drops int-string values
     * @param idList
     * @return
     */
    public static final List<String> getStringList(final List<String> idList) {
        List<String> strList = new ArrayList<String>();
        for (String id : idList) // or sArray
        {
            int iid = getInt(id, -1);
            if(iid == -1) {
                strList.add(id);
            }
        }
        return strList;
    }

    /**
     * Pickups integer values out of String List
     * @param idList
     * @return
     */
    public static final List<Integer> getIntList(final List<String> idList) {
        List<Integer> intList = new ArrayList<Integer>();
        for (String id : idList) // or sArray
        {
            int iid = getInt(id, -1);
            if(iid != -1) {
                intList.add(iid);
            }
        }
        return intList;
    }

    /**
     * Wraps an array of values to a Set of the same type
     * @param array values
     * @return Set wrapped values
     */
    public static <T> Set<T> asSet(final T... array) {
        return new LinkedHashSet<>(Arrays.asList(array));
    }
}
