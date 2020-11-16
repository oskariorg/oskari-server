package java.announcements.actions;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.util.PropertyUtil;

public class AnnouncementsRestActionHandler extends RestActionHandler {
    private final static String PROPERTY_MODULES = "db.additional.modules";

    public void requireAnnouncementsConfigured() throws ActionException {
        String modules = PropertyUtil.get(PROPERTY_MODULES, "");
        if(!modules.contains("announcements")) {
            throw new ActionDeniedException("Announcements not configured");
        }

    }
}
