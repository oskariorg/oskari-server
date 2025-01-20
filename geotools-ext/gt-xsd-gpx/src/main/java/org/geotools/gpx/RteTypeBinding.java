package org.geotools.gpx;


import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xsd.AbstractComplexBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;
import org.geotools.api.feature.Feature;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:rteType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;xsd:complexType name="rteType" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;
 *      &lt;xsd:annotation&gt;
 *        &lt;xsd:documentation&gt;
 *  		rte represents route - an ordered list of waypoints representing a series of turn points leading to a destination.
 *  	  &lt;/xsd:documentation&gt;
 *  	&lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *        &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			GPS name of route.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *  	  &lt;xsd:element minOccurs="0" name="cmt" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			GPS comment for route.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *        &lt;xsd:element minOccurs="0" name="desc" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			Text description of route for user.  Not sent to GPS.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *  	  &lt;xsd:element minOccurs="0" name="src" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			Source of data. Included to give user some idea of reliability and accuracy of data.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *        &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="link" type="linkType"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			Links to external information about the route.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *  	  &lt;xsd:element minOccurs="0" name="number" type="xsd:nonNegativeInteger"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			GPS route number.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *  	  &lt;xsd:element minOccurs="0" name="type" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			Type (classification) of route.
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
 *
 *        &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="rtept" type="wptType"&gt;
 *  	  &lt;xsd:annotation&gt;
 *  	   &lt;xsd:documentation&gt;
 *  		A list of route points.
 *  	   &lt;/xsd:documentation&gt;
 *  	  &lt;/xsd:annotation&gt;
 *  	 &lt;/xsd:element&gt;
 *      &lt;/xsd:sequence&gt;
 *    &lt;/xsd:complexType&gt;
 *
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class RteTypeBinding extends AbstractComplexBinding {

    public static final String FEATURE_TYPE_NAME_ROUTES = "routes";

    private static final String ATTR_NAME_GEOM = "geom";

    private GeometryFactory gf;
    private SimpleFeatureType rteFt;
    private SimpleFeatureBuilder fb;

    public RteTypeBinding(GeometryFactory gf) {
        this.gf = gf;
        SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
        sftb.setName(FEATURE_TYPE_NAME_ROUTES);
        sftb.setDefaultGeometry(ATTR_NAME_GEOM);
        sftb.add(ATTR_NAME_GEOM, LineString.class, DefaultGeographicCRS.WGS84);
        for (GPXField field : GPXField.getCachedValues()) {
            field.addBinding(sftb);
        }
        this.rteFt = sftb.buildFeatureType();
        this.fb = new SimpleFeatureBuilder(rteFt);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.rteType;
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
        fb.set(ATTR_NAME_GEOM, parseRtept(node));
        for (GPXField field : GPXField.getCachedValues()) {
            field.parse(fb, node);
        }
        return fb.buildFeature(null);
    }

    private LineString parseRtept(Node node) {
        List<SimpleFeature> rtepts = node.getChildValues(SimpleFeature.class);
        Coordinate[] ca = new Coordinate[rtepts.size()];
        int i = 0;
        for (SimpleFeature rtept : rtepts) {
            ca[i++] = ((Point) rtept.getDefaultGeometry()).getCoordinate();
        }
        return gf.createLineString(ca);
    }

}
