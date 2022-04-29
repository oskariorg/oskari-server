package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.MetadataHelper;

import java.util.Collections;
import java.util.Set;

import static org.oskari.capabilities.ogc.CapabilitiesConstants.*;

public class LayerCapabilitiesOGC extends LayerCapabilities {

    public static final String VERSION = "version";
    public static final String DESCRIPTION = "desc";
    public static final String KEYWORDS = "keywords";
    public static final String BBOX = "bbox";
    public static final String METADATA_URL = "metadataUrl";
    public static final String METADATA_UUID = "metadataId";
    private BoundingBox bbox;

    public LayerCapabilitiesOGC(String name, String title) {
        super(name, title);
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public void setBbox(BoundingBox bbox) {
        this.bbox = bbox;
    }

    @JsonIgnore
    public String getVersion() {
        return (String) getTypeSpecific().get(VERSION);
    }

    public void setVersion(String ver) {
        addCapabilityData(VERSION, ver);
    }

    public void setFormats(Set<String> formats) {
        addCapabilityData(FORMATS, formats);
    }

    public void setInfoFormats(Set<String> infoFormats) {
        addCapabilityData(INFO_FORMATS, infoFormats);
        // is there any point setting this?
        // isqueryable is NOT used for WMTS currently
        addCapabilityData(IS_QUERYABLE, !infoFormats.isEmpty());
    }

    public void setMetadataUrl(String url) {
        addCapabilityData(METADATA_URL, url);
        if (url != null) {
            addCapabilityData(METADATA_UUID, MetadataHelper.getIdFromMetadataUrl(url));
        }
    }

    @JsonIgnore
    public String getDescription() {
        return (String) getTypeSpecific().get(DESCRIPTION);
    }

    public void setDescription(String description) {
        addCapabilityData(DESCRIPTION, description);
    }

    @JsonIgnore
    public Set<String> getKeywords() {
        return (Set<String>) getTypeSpecific().getOrDefault(KEYWORDS, Collections.emptySet());
    }

    public void setKeywords(Set<String> words) {
        addCapabilityData(KEYWORDS, words);
    }
}
