package fi.nls.oskari.control.admin;

import fi.nls.oskari.util.PropertyUtil;

import java.util.HashMap;
import java.util.Map;

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
