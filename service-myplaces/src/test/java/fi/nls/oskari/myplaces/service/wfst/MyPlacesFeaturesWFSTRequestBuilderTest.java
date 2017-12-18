package fi.nls.oskari.myplaces.service.wfst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.oskari.wfst.response.TransactionResponseParser_100;
import org.oskari.wfst.response.TransactionResponseParser_110;
import org.oskari.wfst.response.TransactionResponse_100;
import org.oskari.wfst.response.TransactionResponse_110;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.util.IOHelper;

public class MyPlacesFeaturesWFSTRequestBuilderTest {

    private static final String PAYLOAD = "payload.json";
    private String endPoint = "http://localhost:6082/geoserver/wms";
    private String contentType = "application/xml";
    private GeometryFactory gf = new GeometryFactory();
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Test
    public void testParseMyPlaces() throws Exception {
        String payload = getInput(PAYLOAD);
        boolean shouldSetId = false;
        List<MyPlace> myPlaces = MyPlacesFeaturesWFSTRequestBuilder.parseMyPlaces(payload, shouldSetId);
        assertEquals(2, myPlaces.size());

        MyPlace myPlace1 = myPlaces.get(0);
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

        MyPlace myPlace2 = myPlaces.get(1);
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
        String payload = getInput(PAYLOAD);
        boolean shouldSetId = true;
        List<MyPlace> myPlaces = MyPlacesFeaturesWFSTRequestBuilder.parseMyPlaces(payload, shouldSetId);
        assertEquals("ids should be set", 123L, myPlaces.get(0).getId());
        assertEquals("ids larger than Integer.MAX work", 9876543210123456L, myPlaces.get(1).getId());
    }

    private String getInput(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            return IOHelper.readString(in);
        }
    }

    @Test
    public void testRemovePrefixFromId() {
        assertEquals(1234L, MyPlacesFeaturesWFSTRequestBuilder.removePrefixFromId("my_places.1234"));
    }

    @Ignore("Requires Geoserver ++ Test codes that isn't run anywhere")
    @Test
    public void testInsertUpdateDelete() throws Exception {
        MyPlace place = new MyPlace();
        List<MyPlace> places = Arrays.asList(place);
        place.setName("foobar");
        place.setUuid(UUID.randomUUID().toString());
        place.setCategoryId(4);
        place.setDesc("My description");
        place.setAttentionText("My attentionText");
        place.setLink("My link");
        place.setGeometry(gf.createPoint(new Coordinate(500000, 6822000)));

        // Insert
        MyPlacesFeaturesWFSTRequestBuilder.insertMyPlaces(baos, places);
        HttpURLConnection conn = IOHelper.post(endPoint, contentType, baos.toByteArray());
        byte[] resp = IOHelper.readBytes(conn);
        TransactionResponse_110 tr = TransactionResponseParser_110.parse(resp);
        assertEquals(1, tr.getTotalInserted());
        assertEquals(0, tr.getTotalUpdated());
        assertEquals(0, tr.getTotalDeleted());
        assertEquals(1, tr.getInsertedFeatures().size());
        String id = tr.getInsertedFeatures().get(0).getFid();
        assertTrue(id.startsWith("my_places."));

        // Update
        long idWithoutPrefix = MyPlacesFeaturesWFSTRequestBuilder.removePrefixFromId(id);
        place.setId(idWithoutPrefix);
        place.setDesc("bazzzz");
        baos.reset();
        MyPlacesFeaturesWFSTRequestBuilder.updateMyPlaces(baos, places);
        conn = IOHelper.post(endPoint, contentType, baos.toByteArray());
        resp = IOHelper.readBytes(conn);
        tr = TransactionResponseParser_110.parse(resp);
        assertEquals(0, tr.getTotalInserted());
        assertEquals(1, tr.getTotalUpdated());
        assertEquals(0, tr.getTotalDeleted());

        // Delete
        long[] ids = { idWithoutPrefix };
        baos.reset();
        MyPlacesFeaturesWFSTRequestBuilder.deleteMyPlaces(baos, ids);
        conn = IOHelper.post(endPoint, contentType, baos.toByteArray());
        resp = IOHelper.readBytes(conn);
        tr = TransactionResponseParser_110.parse(resp);
        assertEquals(0, tr.getTotalInserted());
        assertEquals(0, tr.getTotalUpdated());
        assertEquals(1, tr.getTotalDeleted());
    }

    @Ignore("Requires Geoserver")
    @Test
    public void testInsertUpdateDeletePolygon() throws Exception {
        MyPlace place = new MyPlace();
        List<MyPlace> places = Arrays.asList(place);
        place.setName("foobar");
        place.setUuid(UUID.randomUUID().toString());
        place.setCategoryId(4);
        place.setDesc("My description");
        place.setAttentionText("My attentionText");
        place.setLink("My link");
        Polygon poly = gf.createPolygon(gf.createLinearRing(new Coordinate[] {
                new Coordinate(500000, 6822000),
                new Coordinate(501000, 6822000),
                new Coordinate(501000, 6823000),
                new Coordinate(500000, 6823000),
                new Coordinate(500000, 6822000),
        }));
        place.setGeometry(poly);

        // Insert
        MyPlacesFeaturesWFSTRequestBuilder.insertMyPlaces(baos, places);
        HttpURLConnection conn = IOHelper.post(endPoint, contentType, baos.toByteArray());
        byte[] resp = IOHelper.readBytes(conn);
        TransactionResponse_100 tr = TransactionResponseParser_100.parse(resp);
        assertEquals(1, tr.getInsertedFeatures().size());
        String id = tr.getInsertedFeatures().get(0).getFid();
        assertTrue(id.startsWith("my_places."));

        // Update
        long idWithoutPrefix = MyPlacesFeaturesWFSTRequestBuilder.removePrefixFromId(id);
        place.setId(idWithoutPrefix);
        place.setDesc("bazzzz");
        baos.reset();
        MyPlacesFeaturesWFSTRequestBuilder.updateMyPlaces(baos, places);
        conn = IOHelper.post(endPoint, contentType, baos.toByteArray());
        resp = IOHelper.readBytes(conn);
        tr = TransactionResponseParser_100.parse(resp);
        assertEquals(TransactionResponse_100.Status.SUCCESS, tr.getStatus());

        // Delete
        long[] ids = { idWithoutPrefix };
        baos.reset();
        MyPlacesFeaturesWFSTRequestBuilder.deleteMyPlaces(baos, ids);
        conn = IOHelper.post(endPoint, contentType, baos.toByteArray());
        resp = IOHelper.readBytes(conn);
        tr = TransactionResponseParser_100.parse(resp);
        assertEquals(TransactionResponse_100.Status.SUCCESS, tr.getStatus());
    }

}
