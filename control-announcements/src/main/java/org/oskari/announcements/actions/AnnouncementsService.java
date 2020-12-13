package org.oskari.announcements.actions;

import org.oskari.announcements.helpers.Announcement;
import fi.nls.oskari.service.OskariComponent;
import org.json.JSONObject;

public abstract class AnnouncementsService extends OskariComponent {

    public abstract JSONObject getAnnouncements();
    public abstract JSONObject getAdminAnnouncements();
    public abstract int saveAnnouncement(final Announcement announcement);
    public abstract int updateAnnouncement(final Announcement announcement);
    public abstract int deleteAnnouncement(final int id);

}
