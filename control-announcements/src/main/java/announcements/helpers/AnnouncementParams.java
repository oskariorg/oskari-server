package java.announcements.helpers;

public class AnnouncementParams {
    private String id;
    private String title;
    private Object value;
    private boolean needCastVarchar;


    public AnnouncementParams() {}

    public AnnouncementParams(final String id, final String title, final Object value) {
        this.id = id;
        this.title = title;
        this.value = value;
    }

    public AnnouncementParams(final String id, final String title) {
        this.id = id;
        this.title = title;
    }

    public AnnouncementParams(final String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isNeedCastVarchar() {
        return needCastVarchar;
    }

    public void setNeedCastVarchar(boolean needCastVarchar) {
        this.needCastVarchar = needCastVarchar;
    }
}