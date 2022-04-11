package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.Set;

public class LayerCapabilitiesWFS extends LayerCapabilitiesOGC {

    public static final String OGC_API_CRS_URI = "crs-uri";
    public static final String MAX_FEATURES = CapabilitiesConstants.KEY_MAX_FEATURES;
    public static final String NAMESPACE_URI = "nsUri";

    public LayerCapabilitiesWFS(String name, String title) {
        super(name, title);
    }

    public void setSupportedCrsURIs(Set<String> uris) {
        addCapabilityData(OGC_API_CRS_URI, uris);
    }

    @JsonIgnore
    public Set<String> getSupportedCrsURIs() {
        return (Set<String>) getTypeSpecific().getOrDefault(OGC_API_CRS_URI, Collections.emptySet());
    }

    @JsonIgnore
    public String getNamespaceUri() {
        return (String) getTypeSpecific().getOrDefault(NAMESPACE_URI, null);
    }
    public void setNamespaceUri(String uri) {
        if (uri != null) {
            addCapabilityData(NAMESPACE_URI, uri);
        }
    }
    public void setMaxFeatures(int count) {
        if (count < 0) {
            return;
        }
        try {
            addCapabilityData(MAX_FEATURES, count);
        } catch (Exception ignored) {}
    }
    @JsonIgnore
    public Integer getMaxScale() {
        return (Integer) getTypeSpecific().get(MAX_FEATURES);
    }
}