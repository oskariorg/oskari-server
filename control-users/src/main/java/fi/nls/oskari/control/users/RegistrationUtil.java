package fi.nls.oskari.control.users;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.PropertyUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by SMAKINEN on 1.9.2016.
 */
public class RegistrationUtil {

    public static final String getServerAddress(ActionParameters params) {
        final String domain = PropertyUtil.get("oskari.domain", null);
        if(domain != null) {
            return domain;
        }
        final HttpServletRequest request = params.getRequest();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    /**
     * Create timestamp for 2 days as expirytime.
     * @return
     */
    public static Timestamp createExpiryTime(){
        Calendar calender = Calendar.getInstance();
        Timestamp currentTime = new java.sql.Timestamp(calender.getTime().getTime());
        calender.setTime(currentTime);
        int expireDays = PropertyUtil.getOptional("oskari.email.link.expirytime", 2);
        calender.add(Calendar.DAY_OF_MONTH, expireDays);
        Timestamp expiryTime = new java.sql.Timestamp(calender.getTime().getTime());
        return expiryTime;
    }

    public static boolean isEnabled() {
        return PropertyUtil.getOptional("allow.registration", false);
    }
}
