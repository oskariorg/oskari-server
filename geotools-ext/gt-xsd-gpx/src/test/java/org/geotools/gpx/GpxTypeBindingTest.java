package org.geotools.gpx;


import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.xsd.Binding;
import org.opengis.feature.simple.SimpleFeature;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Binding test case for http://www.topografix.com/GPX/1/1:gpxType.
 *
 * <p>
 *  <pre>
 *   <code>
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
 *    </code>
 *   </pre>
 * </p>
 *
 * @generated
 */
public class GpxTypeBindingTest extends GPXTestSupport {

    public void testType() {
        assertEquals(  FeatureCollection.class, binding( GPX.gpxType ).getType() );
    }

    public void testExecutionMode() {
        assertEquals( Binding.OVERRIDE, binding( GPX.gpxType ).getExecutionMode() );
    }

    public void testParseWpt() throws Exception {
        String xml = "";
        xml += "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\">";
        xml += "<wpt lat=\"10.0\" lon=\"20.0\">";
        xml += "<ele>100.0</ele>";
        xml += "<link href=\"http://my.test.url\">";
        xml += "<text>Here's the text</text>";
        xml += "</link>";
        xml += "</wpt>";
        xml += "</gpx>";
        buildDocument(xml);
        SimpleFeatureCollection result = (SimpleFeatureCollection) parse();
        assertEquals(1, result.size());
        try (SimpleFeatureIterator it = result.features()) {
            SimpleFeature f = it.next();
            assertEquals("http://my.test.url", f.getAttribute(GPXField.LinkHref.getAttribute()));
            assertEquals("Here's the text", f.getAttribute(GPXField.LinkText.getAttribute()));
            Point point = (Point) f.getDefaultGeometry();
            assertEquals(20.0, point.getX(), 0.0);
            assertEquals(10.0, point.getY(), 0.0);
            assertEquals(100.0, point.getCoordinate().z, 0.0);
        }
    }

    public void testParseWpt10() throws Exception {
        String xml = "";
        xml += "<gpx xmlns=\"http://www.topografix.com/GPX/1/0\" version=\"1.0\">";
        xml += "<wpt lat=\"10.0\" lon=\"20.0\">";
        xml += "<ele>100.0</ele>";
        xml += "<url>http://my.test.url</url>";
        xml += "<urlname>Here's the text</urlname>";
        xml += "</wpt>";
        xml += "</gpx>";
        buildDocument(xml);
        SimpleFeatureCollection result = (SimpleFeatureCollection) parse();
        assertEquals(1, result.size());
        try (SimpleFeatureIterator it = result.features()) {
            SimpleFeature f = it.next();
            assertEquals("http://my.test.url", f.getAttribute(GPXField.LinkHref.getAttribute()));
            assertEquals("Here's the text", f.getAttribute(GPXField.LinkText.getAttribute()));
            Point point = (Point) f.getDefaultGeometry();
            assertEquals(20.0, point.getX(), 0.0);
            assertEquals(10.0, point.getY(), 0.0);
            assertEquals(100.0, point.getCoordinate().z, 0.0);
        }
    }

    public void testParseTrk() throws Exception {
        String xml = "";
        xml += "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\">";
        xml += "<trk>";
        xml += "<name>foo</name>";
        xml += "<cmt>bar</cmt>";
        xml += "<desc>baz</desc>";
        xml += "<src>qux</src>";
        xml += "<trkseg>";
        xml += "<trkpt lat=\"10.0\" lon=\"20.0\"><ele>100.0</ele></trkpt>";
        xml += "<trkpt lat=\"20.0\" lon=\"10.0\"><ele>50.0</ele></trkpt>";
        xml += "</trkseg>";
        xml += "<trkseg>";
        xml += "<trkpt lat=\"-10.0\" lon=\"-20.0\"><ele>-100.0</ele></trkpt>";
        xml += "<trkpt lat=\"-20.0\" lon=\"-10.0\"><ele>-50.0</ele></trkpt>";
        xml += "</trkseg>";
        xml += "</trk>";
        xml += "<trk>";
        xml += "<trkseg>";
        xml += "<trkpt lat=\"1.0\" lon=\"2.0\"><ele>3.0</ele></trkpt>";
        xml += "<trkpt lat=\"2.0\" lon=\"1.0\"><ele>4.0</ele></trkpt>";
        xml += "</trkseg>";
        xml += "</trk>";
        xml += "</gpx>";
        buildDocument(xml);
        SimpleFeatureCollection result = (SimpleFeatureCollection) parse();
        assertEquals(2, result.size());
        try (SimpleFeatureIterator it = result.features()) {
            SimpleFeature f = null;
            while (it.hasNext()) {
                f = it.next();
                if ("foo".equals(f.getAttribute("name"))) {
                    break;
                }
            }
            MultiLineString mls = (MultiLineString) f.getDefaultGeometry();
            assertEquals(2, mls.getNumGeometries());
            LineString ls;
            Coordinate c;
            ls = (LineString) mls.getGeometryN(0);
            c = ls.getCoordinateN(0);

            assertEquals(20.0, c.getOrdinate(0), 0.0);
            assertEquals(10.0, c.getOrdinate(1), 0.0);
            assertEquals(100.0, c.getOrdinate(2), 0.0);
            c = ls.getCoordinateN(1);
            assertEquals(10.0, c.getOrdinate(0), 0.0);
            assertEquals(20.0, c.getOrdinate(1), 0.0);
            assertEquals(50.0, c.getOrdinate(2), 0.0);

            ls = (LineString) mls.getGeometryN(1);
            c = ls.getCoordinateN(0);
            assertEquals(-20.0, c.getOrdinate(0), 0.0);
            assertEquals(-10.0, c.getOrdinate(1), 0.0);
            assertEquals(-100.0, c.getOrdinate(2), 0.0);
            c = ls.getCoordinateN(1);
            assertEquals(-10.0, c.getOrdinate(0), 0.0);
            assertEquals(-20.0, c.getOrdinate(1), 0.0);
            assertEquals(-50.0, c.getOrdinate(2), 0.0);

        }
    }

    public void testParseRte() throws Exception {
        String xml = "";
        xml += "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\">";
        xml += "<rte>";
        xml += "<name>foo</name>";
        xml += "<cmt>bar</cmt>";
        xml += "<desc>baz</desc>";
        xml += "<src>qux</src>";
        xml += "<rtept lat=\"10.0\" lon=\"20.0\"><ele>100.0</ele></rtept>";
        xml += "<rtept lat=\"20.0\" lon=\"10.0\"><ele>50.0</ele></rtept>";
        xml += "</rte>";
        xml += "<rte>";
        xml += "<rtept lat=\"1.0\" lon=\"2.0\"><ele>3.0</ele></rtept>";
        xml += "<rtept lat=\"2.0\" lon=\"1.0\"><ele>4.0</ele></rtept>";
        xml += "</rte>";
        xml += "</gpx>";
        buildDocument(xml);
        SimpleFeatureCollection result = (SimpleFeatureCollection) parse();
        assertEquals(2, result.size());
        try (SimpleFeatureIterator it = result.features()) {
            SimpleFeature f = null;
            while (it.hasNext()) {
                f = it.next();
                if ("foo".equals(f.getAttribute("name"))) {
                    break;
                }
            }
            assertNotNull(f);
            assertEquals("bar", f.getAttribute("cmt"));
            assertEquals("baz", f.getAttribute("desc"));
            assertEquals("qux", f.getAttribute("src"));
            LineString ls = (LineString) f.getDefaultGeometry();
            assertEquals(2, ls.getNumPoints());
            Coordinate c;
            c = ls.getCoordinateN(0);
            assertEquals(20.0, c.getOrdinate(0), 0.0);
            assertEquals(10.0, c.getOrdinate(1), 0.0);
            assertEquals(100.0, c.getOrdinate(2), 0.0);
            c = ls.getCoordinateN(1);
            assertEquals(10.0, c.getOrdinate(0), 0.0);
            assertEquals(20.0, c.getOrdinate(1), 0.0);
            assertEquals(50.0, c.getOrdinate(2), 0.0);
        }
    }

}
