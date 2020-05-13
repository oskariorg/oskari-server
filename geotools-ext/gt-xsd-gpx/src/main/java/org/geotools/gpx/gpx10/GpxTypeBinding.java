package org.geotools.gpx.gpx10;

import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Binding object for the type http://www.topografix.com/GPX/1/0:gpxType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;xsd:complexType name="gpxType" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;
 *      &lt;xsd:annotation&gt;
 *        &lt;xsd:documentation&gt;
 *  		GPX documents contain a metadata header, followed by waypoints, routes, and tracks.  You can add your own elements
 *  		to the extensions section of the GPX document.
 *  	  &lt;/xsd:documentation&gt;
 *  	&lt;/xsd:annotation&gt;
 *  	&lt;xsd:sequence&gt;
 *  	 &lt;xsd:element minOccurs="0" name="metadata" type="metadataType"&gt;
 *  	  &lt;xsd:annotation&gt;
 *  	   &lt;xsd:documentation&gt;
 *  		Metadata about the file.
 *  	   &lt;/xsd:documentation&gt;
 *  	  &lt;/xsd:annotation&gt;
 *  	 &lt;/xsd:element&gt;
 *  	 &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="wpt" type="wptType"&gt;
 *  	  &lt;xsd:annotation&gt;
 *  	   &lt;xsd:documentation&gt;
 *  		A list of waypoints.
 *  	   &lt;/xsd:documentation&gt;
 *  	  &lt;/xsd:annotation&gt;
 *  	 &lt;/xsd:element&gt;
 *  	 &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="rte" type="rteType"&gt;
 *  	  &lt;xsd:annotation&gt;
 *  	   &lt;xsd:documentation&gt;
 *  		A list of routes.
 *  	   &lt;/xsd:documentation&gt;
 *  	  &lt;/xsd:annotation&gt;
 *  	 &lt;/xsd:element&gt;
 *  	 &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="trk" type="trkType"&gt;
 *  	  &lt;xsd:annotation&gt;
 *  	   &lt;xsd:documentation&gt;
 *  		A list of tracks.
 *  	   &lt;/xsd:documentation&gt;
 *  	  &lt;/xsd:annotation&gt;
 *  	 &lt;/xsd:element&gt;
 *  	 &lt;xsd:element minOccurs="0" name="extensions" type="extensionsType"&gt;
 *        &lt;xsd:annotation&gt;
 *         &lt;xsd:documentation&gt;
 *  		You can add extend GPX by adding your own elements from another schema here.
 *  	   &lt;/xsd:documentation&gt;
 *  	  &lt;/xsd:annotation&gt;
 *  	 &lt;/xsd:element&gt;
 *  	&lt;/xsd:sequence&gt;
 *
 *  	&lt;xsd:attribute fixed="1.1" name="version" type="xsd:string" use="required"&gt;
 *       &lt;xsd:annotation&gt;
 *        &lt;xsd:documentation&gt;
 *  		You must include the version number in your GPX document.
 *  	  &lt;/xsd:documentation&gt;
 *  	 &lt;/xsd:annotation&gt;
 *  	&lt;/xsd:attribute&gt;
 *  	&lt;xsd:attribute name="creator" type="xsd:string" use="required"&gt;
 *       &lt;xsd:annotation&gt;
 *        &lt;xsd:documentation&gt;
 *  		You must include the name or URL of the software that created your GPX document.  This allows others to
 *  		inform the creator of a GPX instance document that fails to validate.
 *  	  &lt;/xsd:documentation&gt;
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
public class GpxTypeBinding extends AbstractComplexBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX10.gpxType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return FeatureCollection.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
            throws Exception {
        List<SimpleFeature> childValues = node.getChildValues(SimpleFeature.class);
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        featureCollection.addAll(childValues);
        return featureCollection;
    }

}