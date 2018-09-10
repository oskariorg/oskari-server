package fi.nls.oskari.myplaces.service.wfst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.myplaces.service.MyPlacesLayersService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

public class MyPlacesFeaturesServiceWFSTTest {

    @Test
    @Ignore("Modifies properties, requires GeoServer")
    public void testEPSG4326() throws Exception {
        PropertyUtil.addProperty("myplaces.ows.url", "http://localhost:6082/geoserver/ows?", true);
        PropertyUtil.addProperty("myplaces.user", "admin", true);
        PropertyUtil.addProperty("myplaces.password", "${password}", true);

        MyPlacesLayersService layerService = new MyPlacesLayersServiceWFST();
        MyPlacesFeaturesService featureService = new MyPlacesFeaturesServiceWFST();

        MyPlaceCategory myCategory = new MyPlaceCategory();
        myCategory.setCategory_name("testCategory");
        assertEquals(0L, myCategory.getId());

        layerService.insert(Arrays.asList(myCategory));
        long categoryId = myCategory.getId();
        assertNotEquals(0L, categoryId);

        List<MyPlace> places = parseMyPlaces();
        assertEquals(1, places.size());
        assertEquals(4326, places.get(0).getGeometry().getSRID());
        places.get(0).setCategoryId(categoryId);

        MultiLineString mls = (MultiLineString) places.get(0).getGeometry();
        LineString ls = (LineString) mls.getGeometryN(0);
        assertEquals(4, ls.getNumPoints());
        Point p = ls.getPointN(0);
        assertEquals(12.228479709207505, p.getX(), 0.00000001);
        assertEquals(52.00590736899049, p.getY(), 0.00000001);
        p = ls.getPointN(1);
        assertEquals(27.785120334207505, p.getX(), 0.00000001);
        assertEquals(54.46684486899049, p.getY(), 0.00000001);
        p = ls.getPointN(2);
        assertEquals(28.664026584207505, p.getX(), 0.00000001);
        assertEquals(48.31450111899049, p.getY(), 0.00000001);
        p = ls.getPointN(3);
        assertEquals(15.788050021707505, p.getX(), 0.00000001);
        assertEquals(47.69926674399049, p.getY(), 0.00000001);

        long[] featureIds = featureService.insert(places);

        JSONObject featureCollection = featureService.getFeaturesByCategoryId(myCategory.getId(), "EPSG:4326");
        JSONArray features = featureCollection.getJSONArray("features");
        assertEquals(1, features.length());
        JSONObject feature = features.getJSONObject(0);
        assertEquals(featureIds[0], Long.parseLong(feature.getString("id"))); 

        JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");
        assertEquals("MultiLineString", geometry.getString("type"));
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        JSONArray lineString = coordinates.getJSONArray(0);
        assertEquals(4, lineString.length());
        for (int i = 0; i < 4; i++) {
            Point expectedPoint = ls.getPointN(i);
            JSONArray actualPoint = lineString.getJSONArray(i);
            assertEquals(expectedPoint.getX(), actualPoint.getDouble(0), 0.0001); // Allow some transformation error
            assertEquals(expectedPoint.getY(), actualPoint.getDouble(1), 0.0001);
        }

        int numDeleted = featureService.delete(featureIds);
        assertEquals(1, numDeleted);

        numDeleted = layerService.delete(new long[] { myCategory.getId() });
        assertEquals(1, numDeleted);
    }

    private List<MyPlace> parseMyPlaces() throws JSONException, IOException {
        byte[] payloadBytes;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("payload_4326.json")) {
            payloadBytes = IOHelper.readBytes(in);
        }
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        return MyPlacesFeaturesWFSTRequestBuilder.parseMyPlaces(payload, false);
    }

}
