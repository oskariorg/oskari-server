package fi.nls.oskari.domain.map.wfs;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class WFSLayerAttributesTest {

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
        assertTrue("No attributes selected", attrs.getSelectedAttributes().isEmpty());
        assertEquals("No featurecount set", -1, attrs.getMaxFeatures());
        assertNull("No url set", attrs.getNamespaceURL());
        assertNull("Params was null", attrs.getAttributes());
        assertFalse("Filter wasn't given", attrs.hasFilter());
        assertFalse("Localization wasn't given", attrs.getLocalization("en").isPresent());
    }
// Work-in-progress:
    @Test
    public void testSimpleFilter() throws JSONException {
        WFSLayerAttributes attrs = new WFSLayerAttributes(new JSONObject(attributesSimpleFilter));
        System.out.println(attrs.getSelectedAttributes().size());
        System.out.println(attrs.getMaxFeatures());
        System.out.println(attrs.getNamespaceURL());
        System.out.println(attrs.getAttributes());
        System.out.println(attrs.hasFilter());
        System.out.println(attrs.getLocalization("en").isPresent());
    }

    @Test
    public void testLocalizedFilter() throws JSONException {
        WFSLayerAttributes attrs = new WFSLayerAttributes(new JSONObject(attributesLocalizedFilter));
        System.out.println(attrs.getSelectedAttributes().size());
        System.out.println(attrs.getMaxFeatures());
        System.out.println(attrs.getNamespaceURL());
        System.out.println(attrs.getAttributes());
        System.out.println(attrs.hasFilter());
        System.out.println(attrs.getLocalization("en").isPresent());
    }
}