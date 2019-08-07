package fi.nls.oskari.domain.map;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;

public class Feature {
    private String id;
    private HashMap<String, String> properties;
    private String layerName;
    private String namespace;
    private String namespaceURI;
    private Geometry geometry;
    private String GMLGeometryProperty;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String name, String value) {
        if(this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(name, value);
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public boolean hasGeometry() {
        return this.geometry != null;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public String getGMLGeometryProperty() {
        return GMLGeometryProperty;
    }

    public void setGMLGeometryProperty(String GMLGeometryProperty) {
        this.GMLGeometryProperty = GMLGeometryProperty;
    }
}
