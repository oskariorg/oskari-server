package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class LayerCapabilitiesWFS extends LayerCapabilitiesOGC {

    private Collection<FeaturePropertyType> featureProperties;
    private Set<String> crsUris;
    private int maxFeatures = -1;
    private String namespaceUri;
    private String geomName;

    public LayerCapabilitiesWFS(@JsonProperty("name") String name, @JsonProperty("title") String title) {
        super(name, title);
        setType(OskariLayer.TYPE_WFS);
    }

    public Collection<FeaturePropertyType> getFeatureProperties() {
        if (featureProperties == null) {
            return Collections.emptyList();
        }
        return featureProperties;
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

    @JsonProperty("crs-uri")
    public Set<String> getSupportedCrsURIs() {
        if (crsUris == null) {
            return Collections.emptySet();
        }
        return crsUris;
    }

    @JsonProperty("crs-uri")
    public void setSupportedCrsURIs(Set<String> uris) {
        crsUris = uris;
    }

    @JsonIgnore
    public FeaturePropertyType getFeatureProperty(String name) {
        if (name == null) {
            return null;
        }
        return getFeatureProperties().stream().filter(p -> name.equals(p.name)).findFirst().orElse(null);
    }

    @JsonProperty("geomName")
    public String getGeometryField() {
        return geomName;
    }

    @JsonProperty("geomName")
    public void setGeometryField(String geomName) {
        this.geomName = geomName;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceURL) {
        this.namespaceUri = namespaceURL;
    }

    public int getMaxFeatures() {
        return maxFeatures;
    }

    public void setMaxFeatures(int count) {
        maxFeatures = count;
    }
}
