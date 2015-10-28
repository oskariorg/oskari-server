package fi.nls.oskari.search.util;

import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Geometry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GeonetworkSpatialOperation extends SpatialOperation {	
	String polygon;
	List<String> polygonCoordinates; // coordinates in alphabetical order
	
    public GeonetworkSpatialOperation( int operatorId, PropertyName propertyName, Geometry geometry, String polygon ) {
        super( operatorId, propertyName, geometry );
        setPolygon(polygon);
    }
    
    public String getPolygon() {
		return polygon;
	}
    
	public void setPolygon(String polygon) {
		this.polygon = polygon;
		setPolygonCoordinates();
	}
	
	@Override
    public StringBuffer to110XML() {
        StringBuffer sb = new StringBuffer( 2000 );
        sb.append( "<ogc:" ).append( getOperatorName() );
        sb.append( " xmlns:gml='http://www.opengis.net/gml' " ).append( ">" );
        
        if ( super.getPropertyName() != null ) {
            sb.append( super.getPropertyName().toXML() );
        }
        
        sb.append( "<gml:Envelope xmlns:gml=\"http://www.opengis.net/gml\">" );
        sb.append( "<gml:lowerCorner>" + getLowerCorner() + "</gml:lowerCorner>" );
        sb.append( "<gml:upperCorner>" + getUpperCorner() + "</gml:upperCorner>" );
        sb.append( "</gml:Envelope>" );
        
        if ( super.getDistance() > 0 ) {
            sb.append( "<ogc:Distance units=\"m\">" ).append( super.getDistance() ).append( "</ogc:Distance>" );
        }
        sb.append( "</ogc:" ).append( getOperatorName() ).append( ">" );
        return sb;
    }
	
	private void setPolygonCoordinates() {
		// example of a polygon:
		// POLYGON((24.5280896299567 59.8960551267897,24.4930810531741 60.3593423334552,24.9143494472203 60.3666149208281,24.9434863145067 59.9031934632774,24.5280896299567 59.8960551267897))
		// text between POLYGON((...)) is split
		polygonCoordinates = Arrays.asList(polygon.replaceAll("POLYGON\\(\\(", "").replaceAll("\\)\\)", "").split(","));
		Collections.sort(polygonCoordinates);
	}
	
	private String getLowerCorner() {
		return polygonCoordinates.get(0);
	}
	
	private String getUpperCorner() {
		return polygonCoordinates.get(polygonCoordinates.size()-1);
	}
}
