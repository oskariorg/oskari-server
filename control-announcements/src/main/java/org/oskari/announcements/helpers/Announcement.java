package org.oskari.announcements.helpers;

public class Announcement {
    private Integer id;
    private String title;
    private String content;
    private String begin_date;
    private String end_date;
    private Boolean active;
    
    public Announcement() {}

    public Announcement(final Integer id, final String title, final String content, final String begin_date, final String end_date, final Boolean active) {
        this.id = id;
        this.title = title;
        this.content = content;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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