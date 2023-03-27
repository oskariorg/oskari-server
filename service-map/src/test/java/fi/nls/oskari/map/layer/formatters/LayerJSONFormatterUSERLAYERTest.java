package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LayerJSONFormatterUSERLAYERTest {
    private final static LayerJSONFormatterUSERLAYER FORMATTER_USERLAYER = new LayerJSONFormatterUSERLAYER();
    private final static String SRS = "EPSG:4326";
    private final static String LANG = PropertyUtil.getDefaultLanguage();
    private final static String NAMESPACE = "http://www.oskari.org";

    private static final String FIELDS = "[{" +
            "\"name\": \"the_geom\"," +
            "\"type\": \"MultiPolygon\"" +
        "}, {" +
            "\"name\": \"NAME\"," +
            "\"type\": \"String\"," +
            "\"locales\": {" +
                "\"en\": \"Name\"," +
                "\"fi\": \"Nimi\"" +
            "}" +
        "}, {" +
            "\"name\": \"CODE\"," +
            "\"type\": \"Long\","+
            "\"locales\": {" +
                "\"en\": \"Code\"," +
                "\"fi\": \"Koodi\"" +
        "}" +
    "}]";

    @Test
    public void parseFields() throws JSONException {
        OskariLayer baseLayer = new OskariLayer();
        baseLayer.setInternal(true);
        baseLayer.getAttributes().put("namespaceURL", NAMESPACE);
        UserLayer ulayer = new UserLayer();
        ulayer.setFields(new JSONArray(FIELDS));
        JSONObject layerJson = FORMATTER_USERLAYER.getJSON(baseLayer, ulayer, SRS, LANG);
        WFSLayerAttributes attr = new WFSLayerAttributes(layerJson.getJSONObject("attributes"));
        Assert.assertEquals( "parseFields shouldn't override baselayer attributes", NAMESPACE, attr.getNamespaceURL());

        List<String> selected = attr.getSelectedAttributes();
        Assert.assertEquals("2 attributes selected", 2, selected.size());
        Assert.assertEquals("NAME should be first", "NAME", selected.get(0));
        Assert.assertEquals( "CODE should be second","CODE", selected.get(1));

        JSONObject en = attr.getLocalization().get();
        Assert.assertEquals(2, en.length());
        Assert.assertEquals("Name", en.getString("NAME"));
        JSONObject fi = attr.getLocalization("fi").get();
        Assert.assertEquals(2, fi.length());
        Assert.assertEquals("Nimi", fi.getString("NAME"));

        JSONObject data = attr.getAttributes().getJSONObject("data");
        JSONObject types = data.optJSONObject("types");
        Assert.assertEquals("string", types.getString("NAME"));
        Assert.assertEquals("number", types.getString("CODE"));

        Assert.assertEquals("the_geom", data.getString("geometryName"));
        Assert.assertEquals("MultiPolygon", data.getString("geometryType"));
    }

    @Test
    public void parseAttributes() throws JSONException {
        OskariLayer baseLayer = new OskariLayer();
        baseLayer.setInternal(true);
        baseLayer.getAttributes().put("namespaceURL", NAMESPACE);
        UserLayer ulayer = new UserLayer();
        JSONArray fields = new JSONArray(FIELDS);
        ulayer.setFields(fields);
        JSONObject json = FORMATTER_USERLAYER.getJSON(baseLayer, ulayer, SRS, LANG);
        fields.remove(2);
        JSONObject anotherJson = FORMATTER_USERLAYER.getJSON(baseLayer, ulayer, SRS, LANG);

        WFSLayerAttributes attr = new WFSLayerAttributes(json.getJSONObject("attributes"));
        WFSLayerAttributes anotherAttr = new WFSLayerAttributes(anotherJson.getJSONObject("attributes"));

        Assert.assertEquals(attr.getNamespaceURL(), anotherAttr.getNamespaceURL());
        Assert.assertEquals(2, attr.getSelectedAttributes().size());
        Assert.assertEquals(1, anotherAttr.getSelectedAttributes().size());
    }
}
