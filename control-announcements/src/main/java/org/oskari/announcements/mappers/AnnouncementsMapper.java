package org.oskari.announcements.mappers;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.ibatis.annotations.*;

import org.oskari.announcements.model.Announcement;

public interface AnnouncementsMapper {
    @Results(id = "AnnouncementResult", value = {
            @Result(property="id", column="id", id=true),
            @Result(property="beginDate", column="begin_date", javaType=OffsetDateTime.class),
            @Result(property="endDate", column="end_date", javaType=OffsetDateTime.class),
            @Result(property="options", column="options"),
            @Result(property="locale", column="locale")
    })
    @Select("SELECT id, locale, begin_date, end_date, options"
            + " FROM oskari_announcements"
            + " WHERE begin_date <= now() AND end_date >= now()"
            + " ORDER BY id desc")
    List<Announcement> getActiveAnnouncements();

    @ResultMap("AnnouncementResult")
    @Select("SELECT id, locale, begin_date, end_date, options"
            + " FROM oskari_announcements"
            + " ORDER BY id desc")
    List<Announcement> getAnnouncements();

    @Select("DELETE FROM oskari_announcements"
            + " WHERE id = #{id}"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    int deleteAnnouncement(@Param("id") final int id);

    @Select("INSERT INTO oskari_announcements"
            + " (locale, begin_date, end_date, options) VALUES"
            + " (#{locale}, #{beginDate}, #{endDate}, #{options})"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    int saveAnnouncement(final Announcement announcement);

    @Select("UPDATE oskari_announcements"
            + " SET locale = #{locale}, begin_date = #{beginDate},"
            + " end_date = #{endDate} , options = #{options}"
            + " WHERE id = #{id}"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    int updateAnnouncement(final Announcement announcement);
}