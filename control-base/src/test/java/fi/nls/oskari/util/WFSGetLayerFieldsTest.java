package fi.nls.oskari.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.map.OskariLayer;
import org.json.JSONObject;
import org.junit.Test;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class WFSGetLayerFieldsTest {
    private final String LAYER_NAME = "layer-name";
    private final String LAYER_URL = "https://example.com/";
    private final String USERNAME = "username";
    private final String PASSWORD = "pwd";

    @Test
    public void getLayerFieldsForWFS3Collections() throws Exception {
        final OskariLayer layer = getWFSLayer("3.0.0");
        final JSONObject fields = WFSGetLayerFields.getLayerFields(layer);
        assertEquals(fields.getString("geometryName"), "geometry");
        final JSONObject attributes = fields.getJSONObject("types");
        assertEquals(attributes.getString("fid"), "number");
        assertEquals(attributes.getString("attr-1"), "string");
        assertEquals(attributes.getString("attr-2"), "number");
        assertEquals(attributes.getString("attr-3"), "boolean");
        assertEquals(attributes.getString("attr-4"), "unknown");
    }

    @Test
    public void getLayerFieldsForWFSDescribeFeatureType() throws Exception {
        OskariLayer layer = getWFSLayer("2.0.0");
        final JSONObject fields = WFSGetLayerFields.getLayerFields(layer);
        assertEquals(fields.get("geometryName"), "geom");
        final JSONObject attributes = fields.getJSONObject("types");
        assertEquals(attributes.getString("attr-1"), "number");
        assertEquals(attributes.getString("attr-2"), "string");
        assertEquals(attributes.getString("attr-3"), "number");
        assertEquals(attributes.getString("attr-4"), "string");
        assertEquals(attributes.getString("attr-5"), "unknown");
    }

    private List<FeaturePropertyType> getOGCProps() {
        Map<String, String> props = new HashMap<>();
        props.put("geometry", "Polygon");
        props.put("fid", "number");
        props.put("attr-1", "string");
        props.put("attr-2", "number");
        props.put("attr-3", "boolean");
        props.put("attr-4", "unknown");
        return props.keySet().stream().map(key -> {
            FeaturePropertyType prop = new FeaturePropertyType();
            prop.name = key;
            prop.type = props.get(key);
            return prop;
        }).collect(Collectors.toList());
    }

    private List<FeaturePropertyType> getWFSProps() {
        Map<String, String> props = new HashMap<>();
        props.put("geom", "GeometryPropertyType");
        props.put("attr-1", "int");
        props.put("attr-2", "string");
        props.put("attr-3", "double");
        props.put("attr-4", "date");
        props.put("attr-5", "complex");
        return props.keySet().stream().map(key -> {
            FeaturePropertyType prop = new FeaturePropertyType();
            prop.name = key;
            prop.type = props.get(key);
            return prop;
        }).collect(Collectors.toList());
    }

    private OskariLayer getWFSLayer(String version) throws Exception {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setId(1);
        layer.setUrl(LAYER_URL);
        layer.setName(LAYER_NAME);
        layer.setUsername(USERNAME);
        layer.setPassword(PASSWORD);
        layer.setVersion(version);
        LayerCapabilitiesWFS caps = new LayerCapabilitiesWFS(LAYER_NAME, LAYER_NAME);
        if (version.equals("3.0.0")) {
            caps.setFeatureProperties(getOGCProps());
        } else {
            caps.setFeatureProperties(getWFSProps());
        }
        // CapabilitiesService.toJSON(caps, Collections.emptySet())
        String json = new ObjectMapper().writeValueAsString(caps);
        layer.setCapabilities(new JSONObject(json));
        return layer;
    }
}
