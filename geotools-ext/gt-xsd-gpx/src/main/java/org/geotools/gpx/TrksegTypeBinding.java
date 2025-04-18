package org.geotools.gpx;


import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.xsd.AbstractComplexBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;
import org.geotools.api.feature.simple.SimpleFeature;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:trksegType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;xsd:complexType name="trksegType" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;
 *     &lt;xsd:annotation&gt;
 *      &lt;xsd:documentation&gt;
 *   	 A Track Segment holds a list of Track Points which are logically connected in order. To represent a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span of track data.
 *      &lt;/xsd:documentation&gt;
 *     &lt;/xsd:annotation&gt;
 *     &lt;xsd:sequence&gt;	&lt;!-- elements must appear in this order --&gt;
 *  	 &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="trkpt" type="wptType"&gt;
 *        &lt;xsd:annotation&gt;
 *         &lt;xsd:documentation&gt;
 *  		A Track Point holds the coordinates, elevation, timestamp, and metadata for a single point in a track.
 *  	   &lt;/xsd:documentation&gt;
 *  	  &lt;/xsd:annotation&gt;
 *  	 &lt;/xsd:element&gt;
 *
 *  	 &lt;xsd:element minOccurs="0" name="extensions" type="extensionsType"&gt;
 *        &lt;xsd:annotation&gt;
 *         &lt;xsd:documentation&gt;
 *  		You can add extend GPX by adding your own elements from another schema here.
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
public class TrksegTypeBinding extends AbstractComplexBinding {

    private GeometryFactory gf;

    public TrksegTypeBinding(GeometryFactory gf) {
        this.gf = gf;
    }


    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.trksegType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return LineString.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
            throws Exception {
        List<SimpleFeature> trkpts = node.getChildValues(SimpleFeature.class);
        Coordinate[] ca = new Coordinate[trkpts.size()];
        int i = 0;
        for (SimpleFeature trkpt : trkpts) {
            ca[i++] = ((Point) trkpt.getDefaultGeometry()).getCoordinate();
        }
        return gf.createLineString(ca);
    }

}
