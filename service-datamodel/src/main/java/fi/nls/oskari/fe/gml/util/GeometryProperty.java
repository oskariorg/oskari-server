package fi.nls.oskari.fe.gml.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.xml.util.Reference;

/*
 * NOTE: CustomDeserializer means any attributes etc are lost by default
 */
public class GeometryProperty extends Reference {
    private Geometry geometry;

    public GeometryProperty() {
    }

    public GeometryProperty(Geometry value) {
        this.geometry = value;

    }

    @JsonIgnore
    public Geometry getGeometry() {
        return geometry;
    }

    @JsonProperty("WKT")
    public String getWKT() {
        return geometry != null ? geometry.toText() : "";
    }

}
