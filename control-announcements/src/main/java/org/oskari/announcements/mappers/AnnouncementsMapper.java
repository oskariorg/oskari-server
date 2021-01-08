package org.oskari.announcements.mappers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;

import org.oskari.announcements.helpers.Announcement;

public interface AnnouncementsMapper {
    @Select("SELECT id, title, content, begin_date, end_date, active"
            + " FROM oskari_announcements"
            + " WHERE begin_date <= #{date} ::DATE AND end_date >= #{date} ::DATE"
            + " ORDER BY id desc")
    List<Map<String,Object>> getAnnouncements(@Param("date") final LocalDate date);

    @Select("SELECT id, title, content, begin_date, end_date, active"
            + " FROM oskari_announcements"
            + " ORDER BY id desc")
    List<Map<String,Object>> getAdminAnnouncements();

    @Select("DELETE FROM oskari_announcements"
            + " WHERE id = #{id}"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    int deleteAnnouncement(@Param("id") final int id);

    @Select("INSERT INTO oskari_announcements"
            + " (title, content, begin_date, end_date, active) VALUES"
            + " (#{title}, #{content}, #{begin_date} ::DATE, #{end_date} ::DATE, #{active})"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    int saveAnnouncement(final Announcement announcement);

    @Select("UPDATE oskari_announcements"
            + " SET title = #{title}, content = #{content}, begin_date = #{begin_date} ::DATE,"
            + " end_date = #{end_date} ::DATE , active = #{active}"
            + " WHERE id = #{id}"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    int updateAnnouncement(final Announcement announcement);
}