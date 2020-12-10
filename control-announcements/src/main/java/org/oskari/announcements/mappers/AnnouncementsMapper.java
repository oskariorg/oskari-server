package org.oskari.announcements.mappers;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.oskari.announcements.helpers.Announcement;

public interface AnnouncementsMapper {
    List<Map<String,Object>> getAnnouncements();
    List<Map<String,Object>> getAdminAnnouncements();
    int deleteAnnouncement(int id);
    int saveAnnouncement(final Announcement announcement);
    int updateAnnouncement(final Announcement announcement);
}
