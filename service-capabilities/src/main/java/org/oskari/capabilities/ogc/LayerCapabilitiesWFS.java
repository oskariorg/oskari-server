package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.Set;

public class LayerCapabilitiesWFS extends LayerCapabilitiesOGC {

    public static final String OGC_API_CRS_URI = "crs-uri";

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
}
