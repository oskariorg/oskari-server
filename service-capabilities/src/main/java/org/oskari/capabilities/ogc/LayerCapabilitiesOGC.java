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

    public LayerCapabilitiesOGC(String name, String title) {
        super(name, title);
    }

    public void setVersion(String ver) {
        if (ver != null) {
            addCapabilityData(VERSION, ver);
        }
    }
    @JsonIgnore
    public String getVersion() {
        return (String) getTypeSpecific().get(VERSION);
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

    public void setBbox(BoundingBox bbox) {
        if (bbox != null) {
            addCapabilityData(BBOX, bbox);
        }
    }

    @JsonIgnore
    public BoundingBox getBbox() {
        return (BoundingBox) getTypeSpecific().get(BBOX);
    }

    public void setMetadataUrl(String url) {
        if (url != null) {
            addCapabilityData(METADATA_URL, url);
            addCapabilityData(METADATA_UUID, MetadataHelper.getIdFromMetadataUrl(url));
        }
    }

    public void setDescription(String description) {
        if (description != null) {
            addCapabilityData(DESCRIPTION, description);
        }
    }

    @JsonIgnore
    public String getDescription() {
        return (String) getTypeSpecific().get(DESCRIPTION);
    }
    public void setKeywords(Set<String> words) {
        addCapabilityData(KEYWORDS, words);
    }

    @JsonIgnore
    public Set<String> getKeywords() {
        return (Set<String>) getTypeSpecific().getOrDefault(KEYWORDS, Collections.emptySet());
    }
}
