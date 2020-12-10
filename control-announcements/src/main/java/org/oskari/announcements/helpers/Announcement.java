package org.oskari.announcements.helpers;

public class Announcement {
    private Integer id;
    private String title;
    private String content;
    private String beginDate;
    private String endDate;
    private String active;
    
    public Announcement() {}

    public Announcement(final Integer id, final String title, final String content, final String beginDate, final String endDate, final String active) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.beginDate = beginDate;
        this.endDate = endDate;
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
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}