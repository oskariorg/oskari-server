package org.oskari.service.wfs.client.geojson;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.oskari.service.wfs.client.geojson.JSONObjectHandler;

public class JSONObjectHandlerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testJSONObjectHandler() throws ParseException {
        String json = ""
                + "{"
                + "'type': 'Feature',"
                + "'id': 'feature.0',"
                + "'properties': {"
                + "  'otherGeometry': {"
                + "   'type': 'LineString',"
                + "   'coordinates': [[1.1, 1.2], [1.3, 1.4]]"
                + "  }"
                + "},"
                + "'geometry': {"
                + "  'type': 'Point',"
                + "  'coordinates': [0.1, 0.1]"
                + "}"
                + "}";
        json = json.replace('\'', '"');
        JSONParser parser = new JSONParser();

        JSONObjectHandler objectHandler = new JSONObjectHandler();
        parser.parse(json, objectHandler);
        Map<String, Object> map = objectHandler.getJSONObject();
        assertEquals("Feature", map.get("type"));
        assertEquals("feature.0", map.get("id"));
        Map<String, Object> properties = (Map<String, Object>) map.get("properties");

        Map<String, Object> otherGeometry = (Map<String, Object>) properties.get("otherGeometry");
        assertEquals("LineString", otherGeometry.get("type"));
        List<List<Number>> coordinates = (List<List<Number>>) otherGeometry.get("coordinates");
        assertEquals(1.1, coordinates.get(0).get(0).doubleValue(), 0.0);
        assertEquals(1.2, coordinates.get(0).get(1).doubleValue(), 0.0);
        assertEquals(1.3, coordinates.get(1).get(0).doubleValue(), 0.0);
        assertEquals(1.4, coordinates.get(1).get(1).doubleValue(), 0.0);

        Map<String, Object> geometry = (Map<String, Object>) map.get("geometry");
        assertEquals("Point", geometry.get("type"));
        List<Number> geometryCoordinates = (List<Number>) geometry.get("coordinates");
        assertEquals(0.1, geometryCoordinates.get(0).doubleValue(), 0.0);
        assertEquals(0.1, geometryCoordinates.get(1).doubleValue(), 0.0);
    }

}
