package flyway.oskari;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class V1_55_2__migrate_wfslayersTest {

    final String params = "{\n" +
            "  \"default\": [\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"],\n" +
            "  \"fi\": [\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"],\n" +
            "  \"sv\": [\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"],\n" +
            "  \"en\": [\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"]\n" +
            "}";
    final String paramsSingle = "{\n" +
            "  \"fi\": [\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"]" +
            "}";
    final String locales = "{ \n" +
            "  \"fi\": [\"Kunta\",\"Ruutu-ID\",\"ID-nro\",\"X-koordinaatti\",\"Y-koordinaatti\",\"Väestö\",\"Miehet\",\"Naiset\",\"Ikä 0-14\",\"Ikä 15-64\",\"Ikä 65+\"],\n" +
            "  \"sv\": [\"Kommun\",\"Rut-ID\",\"ID-nr\",\"X-koordinat\",\"Y-koordinat\",\"Folkmängd\",\"Män\",\"Kvinnor\",\"Ålder 0-14\",\"Ålder 15-64\",\"Ålder 65+\"],\n" +
            "  \"en\": [\"Municipality\",\"Grid ID\",\"ID No.\",\"X-coordinate\",\"Y-coordinate\",\"Population\",\"Male\",\"Female\",\"Age 0-14\",\"Age 15-64\",\"Age 65+\"]\n" +
            "}";

    final String expectedNoData = "{\"randomKey\":\"for testing\",\"maxFeatures\":100,\"namespaceURL\":\"http://oskari.org\"}";
    final String expectedDataMultiLang = "{\"randomKey\":\"for testing\",\"data\":{\"filter\":{\"default\":[\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"],\"fi\":[\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"],\"sv\":[\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"],\"en\":[\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"]},\"locale\":{\"fi\":{\"naiset\":\"Naiset\",\"ykoord\":\"Y-koordinaatti\",\"vaesto\":\"Väestö\",\"ika_65_\":\"Ikä 65+\",\"xkoord\":\"X-koordinaatti\",\"grd_id\":\"Ruutu-ID\",\"miehet\":\"Miehet\",\"kunta\":\"Kunta\",\"ika_0_14\":\"Ikä 0-14\",\"id_nro\":\"ID-nro\",\"ika_15_64\":\"Ikä 15-64\"},\"sv\":{\"naiset\":\"Kvinnor\",\"ykoord\":\"Y-koordinat\",\"vaesto\":\"Folkmängd\",\"ika_65_\":\"Ålder 65+\",\"xkoord\":\"X-koordinat\",\"grd_id\":\"Rut-ID\",\"miehet\":\"Män\",\"kunta\":\"Kommun\",\"ika_0_14\":\"Ålder 0-14\",\"id_nro\":\"ID-nr\",\"ika_15_64\":\"Ålder 15-64\"},\"en\":{\"naiset\":\"Female\",\"ykoord\":\"Y-coordinate\",\"vaesto\":\"Population\",\"ika_65_\":\"Age 65+\",\"xkoord\":\"X-coordinate\",\"grd_id\":\"Grid ID\",\"miehet\":\"Male\",\"kunta\":\"Municipality\",\"ika_0_14\":\"Age 0-14\",\"id_nro\":\"ID No.\",\"ika_15_64\":\"Age 15-64\"}}},\"maxFeatures\":100,\"namespaceURL\":\"http://oskari.org\"}";
    final String expectedSingle = "{\"randomKey\":\"for testing\",\"data\":{\"filter\":[\"kunta\",\"grd_id\",\"id_nro\",\"xkoord\",\"ykoord\",\"vaesto\",\"miehet\",\"naiset\",\"ika_0_14\",\"ika_15_64\",\"ika_65_\"],\"locale\":{\"fi\":{\"naiset\":\"Naiset\",\"ykoord\":\"Y-koordinaatti\",\"vaesto\":\"Väestö\",\"ika_65_\":\"Ikä 65+\",\"xkoord\":\"X-koordinaatti\",\"grd_id\":\"Ruutu-ID\",\"miehet\":\"Miehet\",\"kunta\":\"Kunta\",\"ika_0_14\":\"Ikä 0-14\",\"id_nro\":\"ID-nro\",\"ika_15_64\":\"Ikä 15-64\"}}},\"maxFeatures\":100,\"namespaceURL\":\"http://oskari.org\"}";

    @Test
    public void migrateAttributesMultiLang() throws Exception {
        V1_55_2__migrate_wfslayers migration = new V1_55_2__migrate_wfslayers();
        V1_55_2__migrate_wfslayers.WFSConfig conf = migration.createConfig(params, locales, "http://oskari.org", 123, 100);
        JSONObject current = new JSONObject();
        current.put("randomKey", "for testing");
        V1_55_2__migrate_wfslayers.LayerAttributes attrs =  migration.migrateAttributes(conf, current);
        assertEquals(expectedDataMultiLang, attrs.attrs);
        System.out.println(attrs.attrs);
    }

    @Test
    public void migrateAttributesSingle() throws Exception {
        V1_55_2__migrate_wfslayers migration = new V1_55_2__migrate_wfslayers();
        V1_55_2__migrate_wfslayers.WFSConfig conf = migration.createConfig(paramsSingle, locales, "http://oskari.org", 123, 100);
        JSONObject current = new JSONObject();
        current.put("randomKey", "for testing");
        V1_55_2__migrate_wfslayers.LayerAttributes attrs =  migration.migrateAttributes(conf, current);
        assertEquals(expectedSingle, attrs.attrs);
        System.out.println(attrs.attrs);
    }
    @Test
    public void migrateAttributesNoData() throws Exception {
        V1_55_2__migrate_wfslayers migration = new V1_55_2__migrate_wfslayers();
        V1_55_2__migrate_wfslayers.WFSConfig conf = migration.createConfig(null, locales, "http://oskari.org", 123, 100);
        JSONObject current = new JSONObject();
        current.put("randomKey", "for testing");
        V1_55_2__migrate_wfslayers.LayerAttributes attrs =  migration.migrateAttributes(conf, current);
        assertEquals(expectedNoData, attrs.attrs);
    }
}