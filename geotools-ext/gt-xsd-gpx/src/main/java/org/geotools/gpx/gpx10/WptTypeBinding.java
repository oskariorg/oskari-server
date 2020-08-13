package org.geotools.gpx.gpx10;


import java.math.BigDecimal;

import javax.xml.namespace.QName;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gpx.GPX;
import org.geotools.gpx.GPXField;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xsd.AbstractComplexBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:wptType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;xsd:complexType name="wptType" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;

 *      &lt;xsd:annotation&gt;

 *        &lt;xsd:documentation&gt;

 *  		wpt represents a waypoint, point of interest, or named feature on a map.

 *  	  &lt;/xsd:documentation&gt;

 *  	&lt;/xsd:annotation&gt;

 *      &lt;xsd:sequence&gt;	&lt;!-- elements must appear in this order --&gt;

 *  	  &lt;!-- Position info --&gt;

 *        &lt;xsd:element minOccurs="0" name="ele" type="xsd:decimal"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Elevation (in meters) of the point.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *        &lt;xsd:element minOccurs="0" name="time" type="xsd:dateTime"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Creation/modification timestamp for element. Date and time in are in Univeral Coordinated Time (UTC), not local time! Conforms to ISO 8601 specification for date/time representation. Fractional seconds are allowed for millisecond timing in tracklogs.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="magvar" type="degreesType"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Magnetic variation (in degrees) at the point

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="geoidheight" type="xsd:decimal"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Height (in meters) of geoid (mean sea level) above WGS84 earth ellipsoid.  As defined in NMEA GGA message.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *

 *  	  &lt;!-- Description info --&gt;

 *  	  &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			The GPS name of the waypoint. This field will be transferred to and from the GPS. GPX does not place restrictions on the length of this field or the characters contained in it. It is up to the receiving application to validate the field before sending it to the GPS.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="cmt" type="xsd:string"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			GPS waypoint comment. Sent to GPS as comment.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="desc" type="xsd:string"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			A text description of the element. Holds additional information about the element intended for the user, not the GPS.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="src" type="xsd:string"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Source of data. Included to give user some idea of reliability and accuracy of data.  "Garmin eTrex", "USGS quad Boston North", e.g.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *        &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="link" type="linkType"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Link to additional information about the waypoint.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="sym" type="xsd:string"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Text of GPS symbol name. For interchange with other programs, use the exact spelling of the symbol as displayed on the GPS.  If the GPS abbreviates words, spell them out.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="type" type="xsd:string"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Type (classification) of the waypoint.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *

 *  	  &lt;!-- Accuracy info --&gt;

 *  	  &lt;xsd:element minOccurs="0" name="fix" type="fixType"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Type of GPX fix.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="sat" type="xsd:nonNegativeInteger"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Number of satellites used to calculate the GPX fix.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="hdop" type="xsd:decimal"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Horizontal dilution of precision.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="vdop" type="xsd:decimal"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Vertical dilution of precision.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="pdop" type="xsd:decimal"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Position dilution of precision.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="ageofdgpsdata" type="xsd:decimal"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			Number of seconds since last DGPS update.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *  	  &lt;xsd:element minOccurs="0" name="dgpsid" type="dgpsStationType"&gt;

 *  		&lt;xsd:annotation&gt;

 *  		  &lt;xsd:documentation&gt;

 *  			ID of DGPS station used in differential correction.

 *  		  &lt;/xsd:documentation&gt;

 *  		&lt;/xsd:annotation&gt;

 *  	  &lt;/xsd:element&gt;

 *

 *  	 &lt;xsd:element minOccurs="0" name="extensions" type="extensionsType"&gt;

 *        &lt;xsd:annotation&gt;

 *         &lt;xsd:documentation&gt;

 *  		You can add extend GPX by adding your own elements from another schema here.

 *  	   &lt;/xsd:documentation&gt;

 *  	  &lt;/xsd:annotation&gt;

 *  	 &lt;/xsd:element&gt;

 *      &lt;/xsd:sequence&gt;

 *

 *      &lt;xsd:attribute name="lat" type="latitudeType" use="required"&gt;

 *  	 &lt;xsd:annotation&gt;

 *  	  &lt;xsd:documentation&gt;

 *  		The latitude of the point.  This is always in decimal degrees, and always in WGS84 datum.

 *  	  &lt;/xsd:documentation&gt;

 *  	 &lt;/xsd:annotation&gt;

 *  	&lt;/xsd:attribute&gt;

 *      &lt;xsd:attribute name="lon" type="longitudeType" use="required"&gt;

 *  	 &lt;xsd:annotation&gt;

 *  	  &lt;xsd:documentation&gt;

 *        The longitude of the point.  This is always in decimal degrees, and always in WGS84 datum.

 *      &lt;/xsd:documentation&gt;

 *  	 &lt;/xsd:annotation&gt;

 *  	&lt;/xsd:attribute&gt;

 *    &lt;/xsd:complexType&gt;
 *
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class WptTypeBinding extends AbstractComplexBinding {

    public static final String FEATURE_TYPE_NAME_WAYPOINTS = "waypoints";

    private static final String ATTR_NAME_GEOM = "geom";

    private GeometryFactory gf;
    private SimpleFeatureType wptFt;
    private SimpleFeatureBuilder fb;

    public WptTypeBinding(GeometryFactory gf) {
        this.gf = gf;
        SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
        sftb.setName(FEATURE_TYPE_NAME_WAYPOINTS);
        sftb.setDefaultGeometry(ATTR_NAME_GEOM);
        sftb.add(ATTR_NAME_GEOM, Point.class, DefaultGeographicCRS.WGS84);
        for (GPXField field : GPXField.getCachedValues()) {
            field.addBinding(sftb);
        }
        this.wptFt = sftb.buildFeatureType();
        this.fb = new SimpleFeatureBuilder(wptFt);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX10.wptType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Feature.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
        fb.reset();
        fb.set(ATTR_NAME_GEOM, parsePoint(node));
        for (GPXField field : GPXField.getCachedValues()) {
            field.parse(fb, node);
        }
        return fb.buildFeature(null);
    }

    public Point parsePoint(Node node) {
        Coordinate c = new Coordinate();

        // &lt;xsd:attribute name="lon" type="longitudeType" use="required"&gt;
        c.x = getDoubleFromAttr(node, "lon", 0.0);

        // &lt;xsd:attribute name="lat" type="latitudeType" use="required"&gt;
        c.y = getDoubleFromAttr(node, "lat", 0.0);

        // &lt;xsd:element minOccurs="0" name="ele" type="xsd:decimal"&gt;
        c.z = getDoubleFromChild(node, "ele", 0.0);

        return gf.createPoint(c);
    }

    private static double getDoubleFromAttr(Node node, String name, double fallback) {
        Node attr = node.getAttribute(name);
        return attr == null ? fallback : toDouble(attr.getValue(), fallback);
    }

    private static double getDoubleFromChild(Node node, String name, double fallback) {
        Node child = node.getChild(name);
        return child == null ? fallback : toDouble(child.getValue(), fallback);
    }

    private static double toDouble(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return fallback;
        }
    }

}