package fi.nls.oskari.control.users.model;

import fi.nls.oskari.util.PropertyUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SMAKINEN on 12.6.2017.
 */
public class PasswordRules {

    public static int getMinLength() {
        return PropertyUtil.getOptional("user.passwd.length", 8);
    }

    public static boolean getRequireCase() {
        return PropertyUtil.getOptional("user.passwd.case", true);
    }

    public static Map asMap() {
        Map value = new HashMap();
        value.put("length", getMinLength());
        value.put("case", getRequireCase());
        return value;
    }
}
