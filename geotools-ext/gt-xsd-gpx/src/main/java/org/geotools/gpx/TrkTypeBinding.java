package org.geotools.gpx;


import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:trkType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;xsd:complexType name="trkType" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;
 *      &lt;xsd:annotation&gt;
 *        &lt;xsd:documentation&gt;
 *  		trk represents a track - an ordered list of points describing a path.
 *  	  &lt;/xsd:documentation&gt;
 *  	&lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *        &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			GPS name of track.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *  	  &lt;xsd:element minOccurs="0" name="cmt" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			GPS comment for track.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *        &lt;xsd:element minOccurs="0" name="desc" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			User description of track.
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
 *  			Links to external information about track.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *  	  &lt;xsd:element minOccurs="0" name="number" type="xsd:nonNegativeInteger"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			GPS track number.
 *  		  &lt;/xsd:documentation&gt;
 *  		&lt;/xsd:annotation&gt;
 *  	  &lt;/xsd:element&gt;
 *  	  &lt;xsd:element minOccurs="0" name="type" type="xsd:string"&gt;
 *  		&lt;xsd:annotation&gt;
 *  		  &lt;xsd:documentation&gt;
 *  			Type (classification) of track.
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
 *       &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="trkseg" type="trksegType"&gt;
 *        &lt;xsd:annotation&gt;
 *         &lt;xsd:documentation&gt;
 *  		A Track Segment holds a list of Track Points which are logically connected in order. To represent a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span of track data.
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
public class TrkTypeBinding extends AbstractComplexBinding {

    public static final String FEATURE_TYPE_NAME_TRACKS = "tracks";

    private static final String ATTR_NAME_GEOM = "geom";

    private GeometryFactory gf;
    private SimpleFeatureType trkFt;
    private SimpleFeatureBuilder fb;

    public TrkTypeBinding(GeometryFactory gf) {
        this.gf = gf;
        SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
        sftb.setName(FEATURE_TYPE_NAME_TRACKS);
        sftb.setDefaultGeometry(ATTR_NAME_GEOM);
        sftb.add(ATTR_NAME_GEOM, MultiLineString.class, DefaultGeographicCRS.WGS84);
        for (GPXField field : GPXField.getCachedValues()) {
            field.addBinding(sftb);
        }
        this.trkFt = sftb.buildFeatureType();
        this.fb = new SimpleFeatureBuilder(trkFt);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.trkType;
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
        List<LineString> trackSegments = node.getChildValues(LineString.class);
        MultiLineString geom = gf.createMultiLineString(trackSegments.toArray(new LineString[0]));
        fb.reset();
        fb.set(ATTR_NAME_GEOM, geom);
        for (GPXField field : GPXField.getCachedValues()) {
            field.parse(fb, node);
        }
        return fb.buildFeature(null);
    }

}