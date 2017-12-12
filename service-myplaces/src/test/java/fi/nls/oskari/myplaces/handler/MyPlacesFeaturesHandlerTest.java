package fi.nls.oskari.myplaces.handler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.myplaces.MyPlaceWithGeometry;
import fi.nls.oskari.util.IOHelper;

public class MyPlacesFeaturesHandlerTest {

    private static final String PAYLOAD = "payload.json";

    @Test
    public void testParseMyPlaces() throws Exception {
        MyPlacesFeaturesHandler handler = new MyPlacesFeaturesHandler();
        byte[] payload = getInput(PAYLOAD);

        boolean shouldSetId = false;
        MyPlaceWithGeometry[] myPlaces = handler.parseMyPlaces(payload, shouldSetId);
        assertEquals(2, myPlaces.length);

        MyPlaceWithGeometry myPlace1 = myPlaces[0];
        assertEquals(1, myPlace1.getCategoryId());
        assertEquals("id should not be set because we set shouldSetId to false",
                0, myPlace1.getId());
        assertEquals("name", myPlace1.getName());
        assertEquals("place description", myPlace1.getDesc());
        assertEquals("url", myPlace1.getLink());
        assertEquals("attention text", myPlace1.getAttentionText());
        assertEquals("image url", myPlace1.getImageUrl());
        Geometry g = myPlace1.getGeometry();
        assertEquals("Point", g.getGeometryType());
        Point p = (Point) g;
        assertEquals(381863.4646715279, p.getX(), 0.00000001);
        assertEquals(6679150.155217625, p.getY(), 0.00000001);

        MyPlaceWithGeometry myPlace2 = myPlaces[1];
        assertEquals(1, myPlace2.getCategoryId());
        assertEquals("id should not be set because we set shouldSetId to false",
                0, myPlace1.getId());
        assertEquals("name2", myPlace2.getName());
        assertEquals("place description2", myPlace2.getDesc());
        assertEquals("url2", myPlace2.getLink());
        assertEquals("attention text2", myPlace2.getAttentionText());
        assertEquals("image url2", myPlace2.getImageUrl());
        g = myPlace2.getGeometry();
        assertEquals("MultiLineString", g.getGeometryType());
        MultiLineString ml = (MultiLineString) g;
        assertEquals(2, ml.getNumGeometries());

        LineString ls = (LineString) ml.getGeometryN(0);
        assertEquals(3, ls.getNumPoints());
        p = ls.getPointN(0);
        assertEquals(381863.4646715279, p.getX(), 0.00000001);
        assertEquals(6679150.155217625, p.getY(), 0.00000001);
        p = ls.getPointN(1);
        assertEquals(384807.4646715279, p.getX(), 0.00000001);
        assertEquals(6682990.155217625, p.getY(), 0.00000001);
        p = ls.getPointN(2);
        assertEquals(386599.4646715279, p.getX(), 0.00000001);
        assertEquals(6678510.155217625, p.getY(), 0.00000001);

        ls = (LineString) ml.getGeometryN(1);
        assertEquals(2, ls.getNumPoints());
        p = ls.getPointN(0);
        assertEquals(378215.4646715279, p.getX(), 0.00000001);
        assertEquals(6676878.155217625, p.getY(), 0.00000001);
        p = ls.getPointN(1);
        assertEquals(381639.4646715279, p.getX(), 0.00000001);
        assertEquals(6678318.155217625, p.getY(), 0.00000001);
    }

    @Test
    public void testParseMyPlacesWithIds() throws Exception {
        MyPlacesFeaturesHandler handler = new MyPlacesFeaturesHandler();
        byte[] payload = getInput(PAYLOAD);
        boolean shouldSetId = true;
        MyPlaceWithGeometry[] myPlaces = handler.parseMyPlaces(payload, shouldSetId);
        assertEquals("ids should be set", 123L, myPlaces[0].getId());
        assertEquals("ids larger than Integer.MAX work", 9876543210123456L, myPlaces[1].getId());
    }

    private byte[] getInput(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            return IOHelper.readBytes(in);
        }
    }

}