package org.oskari.announcements.helpers;

@Deprecated
public class Announcement {
    private Integer id;
    private String locale;
    private String begin_date;
    private String end_date;
    private Boolean active;
    
    public Announcement() {}

    public Announcement(final Integer id, final String locale, final String begin_date, final String end_date, final Boolean active) {
        this.id = id;
        this.locale = locale;
        this.begin_date = begin_date;
        this.end_date = end_date;
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getBeginDate() {
        return begin_date;
    }

    public void setBeginDate(String begin_date) {
        this.begin_date = begin_date;
    }

    public String getEndDate() {
        return end_date;
    }

    public void setEndDate(String end_date) {
        this.end_date = end_date;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}