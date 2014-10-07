package fi.nls.oskari.domain.map;

/**
 * Common model for layers consisting of user created data.
 */
public class UserDataLayer {

    private String uuid;
    private String publisher_name;

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPublisher_name() {
        return publisher_name;
    }

    public void setPublisher_name(String publisher_name) {
        this.publisher_name = publisher_name;
    }

    public boolean isPublished() {
        return getPublisher_name() != null;
    }

    public boolean isOwnedBy(final String uuid) {
        if(uuid == null || getUuid() == null) {
            return false;
        }
        return getUuid().equals(uuid);
    }
}
