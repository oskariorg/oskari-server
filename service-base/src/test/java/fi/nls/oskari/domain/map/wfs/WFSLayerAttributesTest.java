package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WFSLayerAttributesTest {

    String simpleAttributes = "{\n" +
            "    \"randomKey\": \"for testing\",\n" +
            "    \"maxFeatures\": 2000,\n" +
            "    \"namespaceURL\": \"http://oskari.org\",\n" +
            "    \"data\":{\n" +
            "       \"noDataValue\":-1,\n" +
            "       \"commonId\":\"grid_id\"\n" +
            "    }\n" +
            "}";

    String attributesSimpleFilter = "{\n" +
            "    \"randomKey\": \"for testing\",\n" +
            "    \"data\": {\n" +
            "        \"filter\": [\n" +
            "            \"kunta\",\n" +
            "            \"grd_id\",\n" +
            "            \"id_nro\",\n" +
            "            \"xkoord\",\n" +
            "            \"ykoord\",\n" +
            "            \"vaesto\",\n" +
            "            \"miehet\",\n" +
            "            \"naiset\",\n" +
            "            \"ika_0_14\",\n" +
            "            \"ika_15_64\",\n" +
            "            \"ika_65_\"\n" +
            "        ],\n" +
            "        \"locale\": {\n" +
            "            \"fi\": {\n" +
            "                \"naiset\": \"Naiset\",\n" +
            "                \"ykoord\": \"Y-koordinaatti\",\n" +
            "                \"vaesto\": \"Väestö\",\n" +
            "                \"ika_65_\": \"Ikä 65+\",\n" +
            "                \"xkoord\": \"X-koordinaatti\",\n" +
            "                \"grd_id\": \"Ruutu-ID\",\n" +
            "                \"miehet\": \"Miehet\",\n" +
            "                \"kunta\": \"Kunta\",\n" +
            "                \"ika_0_14\": \"Ikä 0-14\",\n" +
            "                \"id_nro\": \"ID-nro\",\n" +
            "                \"ika_15_64\": \"Ikä 15-64\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"maxFeatures\": 100,\n" +
            "    \"namespaceURL\": \"http://oskari.org\"\n" +
            "}";

    String attributesLocalizedFilter = "{\n" +
            "    \"randomKey\": \"for testing\",\n" +
            "    \"data\": {\n" +
            "        \"filter\": {\n" +
            "            \"default\": [\n" +
            "                \"kunta\",\n" +
            "                \"grd_id\",\n" +
            "                \"id_nro\",\n" +
            "                \"xkoord\",\n" +
            "                \"ykoord\",\n" +
            "                \"vaesto\",\n" +
            "                \"miehet\",\n" +
            "                \"naiset\",\n" +
            "                \"ika_0_14\",\n" +
            "                \"ika_15_64\",\n" +
            "                \"ika_65_\"\n" +
            "            ],\n" +
            "            \"fi\": [\n" +
            "                \"kunta\",\n" +
            "                \"grd_id\",\n" +
            "                \"id_nro\",\n" +
            "                \"xkoord\",\n" +
            "                \"ykoord\",\n" +
            "                \"vaesto\",\n" +
            "                \"miehet\",\n" +
            "                \"naiset\",\n" +
            "                \"ika_0_14\",\n" +
            "                \"ika_15_64\",\n" +
            "                \"ika_65_\"\n" +
            "            ],\n" +
            "            \"sv\": [\n" +
            "                \"kunta\",\n" +
            "                \"grd_id\",\n" +
            "                \"id_nro\",\n" +
            "                \"xkoord\",\n" +
            "                \"ykoord\",\n" +
            "                \"vaesto\",\n" +
            "                \"miehet\",\n" +
            "                \"naiset\",\n" +
            "                \"ika_0_14\",\n" +
            "                \"ika_15_64\",\n" +
            "                \"ika_65_\"\n" +
            "            ],\n" +
            "            \"en\": [\n" +
            "                \"kunta\",\n" +
            "                \"grd_id\",\n" +
            "                \"id_nro\",\n" +
            "                \"xkoord\",\n" +
            "                \"ykoord\",\n" +
            "                \"vaesto\",\n" +
            "                \"miehet\",\n" +
            "                \"naiset\",\n" +
            "                \"ika_0_14\",\n" +
            "                \"ika_15_64\",\n" +
            "                \"ika_65_\"\n" +
            "            ]\n" +
            "        },\n" +
            "        \"locale\": {\n" +
            "            \"fi\": {\n" +
            "                \"naiset\": \"Naiset\",\n" +
            "                \"ykoord\": \"Y-koordinaatti\",\n" +
            "                \"vaesto\": \"Väestö\",\n" +
            "                \"ika_65_\": \"Ikä 65+\",\n" +
            "                \"xkoord\": \"X-koordinaatti\",\n" +
            "                \"grd_id\": \"Ruutu-ID\",\n" +
            "                \"miehet\": \"Miehet\",\n" +
            "                \"kunta\": \"Kunta\",\n" +
            "                \"ika_0_14\": \"Ikä 0-14\",\n" +
            "                \"id_nro\": \"ID-nro\",\n" +
            "                \"ika_15_64\": \"Ikä 15-64\"\n" +
            "            },\n" +
            "            \"sv\": {\n" +
            "                \"naiset\": \"Kvinnor\",\n" +
            "                \"ykoord\": \"Y-koordinat\",\n" +
            "                \"vaesto\": \"Folkmängd\",\n" +
            "                \"ika_65_\": \"Ålder 65+\",\n" +
            "                \"xkoord\": \"X-koordinat\",\n" +
            "                \"grd_id\": \"Rut-ID\",\n" +
            "                \"miehet\": \"Män\",\n" +
            "                \"kunta\": \"Kommun\",\n" +
            "                \"ika_0_14\": \"Ålder 0-14\",\n" +
            "                \"id_nro\": \"ID-nr\",\n" +
            "                \"ika_15_64\": \"Ålder 15-64\"\n" +
            "            },\n" +
            "            \"en\": {\n" +
            "                \"naiset\": \"Female\",\n" +
            "                \"ykoord\": \"Y-coordinate\",\n" +
            "                \"vaesto\": \"Population\",\n" +
            "                \"ika_65_\": \"Age 65+\",\n" +
            "                \"xkoord\": \"X-coordinate\",\n" +
            "                \"grd_id\": \"Grid ID\",\n" +
            "                \"miehet\": \"Male\",\n" +
            "                \"kunta\": \"Municipality\",\n" +
            "                \"ika_0_14\": \"Age 0-14\",\n" +
            "                \"id_nro\": \"ID No.\",\n" +
            "                \"ika_15_64\": \"Age 15-64\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"maxFeatures\": 100,\n" +
            "    \"namespaceURL\": \"http://oskari.org\"\n" +
            "}";

    @Test
    public void testEmptyParam() {
        WFSLayerAttributes attrs = new WFSLayerAttributes(null);
        Assertions.assertTrue(attrs.getSelectedAttributes().isEmpty(), "No attributes selected");
        Assertions.assertEquals(100000, attrs.getMaxFeatures(), "No featurecount set");
        Assertions.assertNull(attrs.getNamespaceURL(), "No url set");
        Assertions.assertNull(attrs.getAttributes(), "Params was null");
        Assertions.assertFalse(attrs.hasFilter(), "Filter wasn't given");
        Assertions.assertFalse(attrs.getLocalization("en").isPresent(), "Localization wasn't given");
        Assertions.assertNull(attrs.getNoDataValue(), "noDataValue was null");
        Assertions.assertNull(attrs.getCommonId(), "commonId was null");
    }

    @Test
    public void testSimpleFilter() throws JSONException {
        JSONObject input = new JSONObject(attributesSimpleFilter);
        WFSLayerAttributes attrs = new WFSLayerAttributes(input);
        Assertions.assertEquals(11, attrs.getSelectedAttributes().size(), "11 attributes selected");
        Assertions.assertEquals(100, attrs.getMaxFeatures(), "Max features set");
        Assertions.assertEquals("http://oskari.org", attrs.getNamespaceURL(), "Namespace set");
        Assertions.assertTrue(JSONHelper.isEqual(input, attrs.getAttributes()), "Input & output match");
        Assertions.assertTrue(attrs.hasFilter(), "Filter was given");
        Assertions.assertFalse(attrs.getLocalization("en").isPresent(), "English locale wasn't given");
    }

    @Test
    public void testLocalizedFilter() throws JSONException {
        JSONObject input = new JSONObject(attributesLocalizedFilter);
        WFSLayerAttributes attrs = new WFSLayerAttributes(input);

        Assertions.assertEquals(11, attrs.getSelectedAttributes().size(), "11 attributes selected for default lang");
        Assertions.assertEquals(11, attrs.getSelectedAttributes("sv").size(), "11 attributes selected for sv lang");
        Assertions.assertEquals(100, attrs.getMaxFeatures(), "Max features set");
        Assertions.assertEquals("http://oskari.org", attrs.getNamespaceURL(), "Namespace set");
        Assertions.assertTrue(JSONHelper.isEqual(input, attrs.getAttributes()), "Input & output match");
        Assertions.assertTrue(attrs.hasFilter(), "Filter was given");
        Assertions.assertTrue(attrs.getLocalization("en").isPresent(), "English locale wasn't given");
    }
    @Test
    public void testSimpleAttributes() throws JSONException {
        JSONObject input = new JSONObject(simpleAttributes);
        WFSLayerAttributes attrs = new WFSLayerAttributes(input);

        Assertions.assertEquals(2000, attrs.getMaxFeatures(), "Max features set");
        Assertions.assertEquals("http://oskari.org", attrs.getNamespaceURL(), "Namespace set");
        Assertions.assertTrue(JSONHelper.isEqual(input, attrs.getAttributes()), "Input & output match");

        Assertions.assertEquals(-1, attrs.getNoDataValue().intValue(), "noDataValue set");
        Assertions.assertEquals("grid_id", attrs.getCommonId(), "commonId set");

        Assertions.assertFalse(attrs.hasFilter(), "Filter wasn't given");
        Assertions.assertFalse(attrs.getLocalization("en").isPresent(), "Localization wasn't given");
        Assertions.assertTrue(attrs.getSelectedAttributes().isEmpty(), "No attributes selected");
    }
}
