package org.oskari.announcements.actions;

import java.util.List;
import org.oskari.announcements.model.Announcement;
import fi.nls.oskari.service.OskariComponent;

public abstract class AnnouncementsService extends OskariComponent {

    public abstract List<Announcement> getAnnouncements();
    public abstract List<Announcement> getActiveAnnouncements();
    public abstract int saveAnnouncement(final Announcement announcement);
    public abstract int updateAnnouncement(final Announcement announcement);
    public abstract int deleteAnnouncement(final int id);

}
