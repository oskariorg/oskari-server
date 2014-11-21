package fi.nls.oskari.fe.input.jackson;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryProperty {
    
    public Geometry geometry ;
    
    public GeometryProperty() {
        
    }
    
    public GeometryProperty(Geometry value) {
        this.geometry = value;
        
    }
}
