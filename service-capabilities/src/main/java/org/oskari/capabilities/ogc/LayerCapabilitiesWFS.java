package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;

import java.util.*;

public class LayerCapabilitiesWFS extends LayerCapabilitiesOGC {

    public static final String OGC_API_CRS_URI = "crs-uri";
    public static final String MAX_FEATURES = CapabilitiesConstants.KEY_MAX_FEATURES;
    public static final String NAMESPACE_URI = "nsUri"; // previously KEY_NAMESPACE_URL = "namespaceURL";
    public static final String GEOMETRY_FIELD = "geomName";
    private Collection<FeaturePropertyType> featureProperties = new ArrayList<>();

    public LayerCapabilitiesWFS(@JsonProperty("name") String name, @JsonProperty("title") String title) {
        super(name, title);
        setType(OskariLayer.TYPE_WFS);
    }

    public void setFeatureProperties(Collection<FeaturePropertyType> props) {
        if (props != null) {
            featureProperties = props;
            setGeometryField(props.stream()
                    .filter(p -> p.isGeometry())
                    .map(p -> p.name)
                    .findFirst()
                    .orElse(null));
        }
    }

    public Collection<FeaturePropertyType> getFeatureProperties() {
        return featureProperties;
    }
    public void setSupportedCrsURIs(Set<String> uris) {
        addCapabilityData(OGC_API_CRS_URI, uris);
    }

    @JsonIgnore
    public Set<String> getSupportedCrsURIs() {
        return (Set<String>) getTypeSpecific().getOrDefault(OGC_API_CRS_URI, Collections.emptySet());
    }

    @JsonIgnore
    public FeaturePropertyType getFeatureProperty(String name) {
        return getFeatureProperties().stream().filter(p -> name.equals(p.name)).findFirst().orElse(null);
    }

    public void setGeometryField(String geomName) {
        if (geomName != null) {
            addCapabilityData(GEOMETRY_FIELD, geomName);
        }
    }

    @JsonIgnore
    public String getGeometryField() {
        return (String) getTypeSpecific().get(GEOMETRY_FIELD);
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
