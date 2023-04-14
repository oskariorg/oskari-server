package org.oskari.user.util;

import fi.nls.oskari.util.PropertyUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserHelper {

    // From: https://owasp.org/www-community/OWASP_Validation_Regex_Repository
    private static final String EMAIL_REGEXP = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEXP);

    public static boolean isValidEmail(String email) {
        return email != null && !email.isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPasswordOk(String passwd) {
        if (passwd == null) {
            return false;
        }
        if (passwd.length() < getMinLength()) {
            return false;
        }
        if (getRequireCase() &&
                (passwd.toLowerCase().equals(passwd) || passwd.toUpperCase().equals(passwd))) {
            return false;
        }
        return true;
    }
    public static int getMinLength() {
        return PropertyUtil.getOptional("user.passwd.length", 8);
    }

    public static boolean getRequireCase() {
        return PropertyUtil.getOptional("user.passwd.case", true);
    }

    public static Map getPasswordRequirements() {
        Map value = new HashMap();
        value.put("length", getMinLength());
        value.put("case", getRequireCase());
        return value;
    }
}
