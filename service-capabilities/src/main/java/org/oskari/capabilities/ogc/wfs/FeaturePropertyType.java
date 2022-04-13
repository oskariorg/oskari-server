package org.oskari.capabilities.ogc.wfs;

public class FeaturePropertyType {
    public String name;
    public String type;

    public boolean isGeometry() {
        return "GeometryPropertyType".equals(type);
    }
}
