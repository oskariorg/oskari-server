package org.geotools.gpx.gpx10;


import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.xsd.Binding;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import static org.junit.Assert.assertEquals;

public class GPX10Test extends GPX10TestSupport {

    public void testType() {
        assertEquals(  FeatureCollection.class, binding( GPX10.gpxType ).getType() );
    }

    public void testExecutionMode() {
        assertEquals( Binding.OVERRIDE, binding( GPX10.gpxType ).getExecutionMode() );
    }

    @Test
    public void testParseTrk10() throws Exception {
        String xml = "";
        xml += "<gpx version=\"1.0\" creator=\"a developer edited this manually\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">";
        xml += "<trk>";
        xml += "<trkseg>";
        xml += "<trkpt lat=\"62.494332790374756\" lon=\"25.704601407051086\"><ele>152.50732421875</ele><time>2020-03-03T07:59:50.449Z</time><speed>0.0</speed><src>gps</src><sat>7</sat></trkpt>";
        xml += "<trkpt lat=\"62.49435841\" lon=\"25.70474762\"><ele>150.565673828125</ele><time>2020-03-03T08:04:01.000Z</time><course>42.4</course><speed>1.39</speed><geoidheight>20.4</geoidheight><src>gps</src><sat>26</sat><hdop>0.5</hdop><vdop>0.6</vdop><pdop>0.8</pdop></trkpt>";
        xml += "<trkpt lat=\"62.4944432\" lon=\"25.70493199\"><ele>152.8448486328125</ele><time>2020-03-03T08:04:11.000Z</time><course>61.4</course><speed>1.23</speed><geoidheight>20.4</geoidheight><src>gps</src><sat>23</sat><hdop>0.6</hdop><vdop>0.7</vdop><pdop>0.9</pdop></trkpt>";
        xml += "<trkpt lat=\"62.49450983\" lon=\"25.70518417\"><ele>155.17364501953125</ele><time>2020-03-03T08:04:22.000Z</time><course>82.5</course><speed>1.13</speed><geoidheight>20.4</geoidheight><src>gps</src><sat>23</sat><hdop>0.6</hdop><vdop>0.7</vdop><pdop>0.9</pdop></trkpt>";
        xml += "</trkseg>";
        xml += "</trk>";
        xml += "</gpx>";
        buildDocument(xml);
        SimpleFeatureCollection result = (SimpleFeatureCollection) parse();
        assertEquals(1, result.size());
        try (SimpleFeatureIterator it = result.features()) {
            SimpleFeature f = it.next();
            MultiLineString track = (MultiLineString) f.getDefaultGeometry();
            assertEquals(1, track.getNumGeometries());

            LineString tracksegment = (LineString) track.getGeometryN(0);
            CoordinateSequence csq = tracksegment.getCoordinateSequence();
            assertEquals(4, csq.size());

            assertEquals(25.704601407051086, csq.getX(0), 1e-8);
            assertEquals(62.494332790374756, csq.getY(0), 1e-8);
            assertEquals(152.50732421875, csq.getOrdinate(0, 2), 1e-8);

            assertEquals(25.70474762, csq.getX(1), 1e-8);
            assertEquals(62.49435841, csq.getY(1), 1e-8);
            assertEquals(150.565673828125, csq.getOrdinate(1, 2), 1e-8);

            assertEquals(25.70493199, csq.getX(2), 1e-8);
            assertEquals(62.4944432, csq.getY(2), 1e-8);
            assertEquals(152.8448486328125, csq.getOrdinate(2, 2), 1e-8);

            assertEquals(25.70518417, csq.getX(3), 1e-8);
            assertEquals(62.49450983, csq.getY(3), 1e-8);
            assertEquals(155.17364501953125, csq.getOrdinate(3, 2), 1e-8);
        }
    }

}