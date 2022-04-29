package org.oskari.capabilities.ogc;

import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.MetadataHelper;

import java.util.Collections;
import java.util.Set;

public class LayerCapabilitiesOGC extends LayerCapabilities {

    // FIXME: remove constants
    public static final String METADATA_URL = "metadataUrl";
    public static final String METADATA_UUID = "metadataId";
    private BoundingBox bbox;
    private String version;
    private String desc;
    private String metadataUrl;
    private String metadataId;
    private Set<String> formats;
    private Set<String> keywords;

    public LayerCapabilitiesOGC(String name, String title) {
        super(name, title);
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public void setBbox(BoundingBox bbox) {
        this.bbox = bbox;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String ver) {
        version = ver;
    }

    public Set<String> getFormats() {
        if (formats == null) {
            return Collections.emptySet();
        }
        return formats;
    }

    public void setFormats(Set<String> formats) {
        this.formats = formats;
    }

    public String getDesc() {
        return desc;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String url) {
        metadataUrl = url;
        if (url != null) {
            metadataId = MetadataHelper.getIdFromMetadataUrl(url);
        }
    }

    public String getMetadataId() {
        return metadataId;
    }

    public String getDescription() {
        return desc;
    }

    public void setDescription(String description) {
        desc = description;
    }

    public Set<String> getKeywords() {
        if (keywords == null) {
            return Collections.emptySet();
        }
        return keywords;
    }

    public void setKeywords(Set<String> words) {
        this.keywords = words;
    }
}
