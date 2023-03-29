package org.oskari.user.util;

import fi.nls.oskari.util.PropertyUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SMAKINEN on 12.6.2017.
 */
public class UserHelper {

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
