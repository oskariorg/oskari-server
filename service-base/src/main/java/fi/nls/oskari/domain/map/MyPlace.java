package fi.nls.oskari.domain.map;

import org.locationtech.jts.geom.Geometry;

import java.time.OffsetDateTime;

public class MyPlace {
    private long id;
    private String uuid;
    private long categoryId;
    private String name;
    private String desc;
    private String link;
    private String imageUrl;
    private String attentionText;
    private Geometry geometry;

    private String wkt;

    private int databaseSRID;
    private OffsetDateTime created;
    private OffsetDateTime updated;
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAttentionText() {
        return attentionText;
    }

    public void setAttentionText(String attentionText) {
        this.attentionText = attentionText;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
        this.wkt = geometry.toText();
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    public int getApplicationSRID() {
        return this.geometry.getSRID();
    }

    public int getDatabaseSRID() {
        return databaseSRID;
    }

    public void setDatabaseSRID(int databaseSRID) {
        this.databaseSRID = databaseSRID;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public boolean isOwnedBy(final String uuid) {
        if(uuid == null || getUuid() == null) {
            return false;
        }
        return getUuid().equals(uuid);
    }
}
