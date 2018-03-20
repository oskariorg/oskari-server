package flyway.oskari;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class V1_45_4__migrate_thematic_mapsTest {

    private JSONObject getTestIndicator() throws JSONException{
        JSONObject ind = new JSONObject();
        ind.put("ds", 1);
        ind.put("id", 2);
        JSONObject params = new JSONObject();
        params.put("year", "2017");
        params.put("sex", "total");
        ind.put("selections", params);
        return ind;
    }
    @Test
    public void oldHash() throws JSONException {
        // OLD: "currentColumn": "indicator2882013total"
        // "indicator" + id + year + male/female/total
        JSONObject indicator = getTestIndicator();
        assertEquals("indicator22017total", V1_45_4__migrate_thematic_maps.getCurrentColumnStr(indicator));
    }

    @Test
    public void newHash() throws JSONException {
        // NEW: "active" : "1_4_sex="total":year="2016""
        // ds_id + '_' + ind_id + '_' + [alphabetical order for selections] key + '=' + value [separated by] ':'
        JSONObject indicator = getTestIndicator();
        assertEquals("1_2_sex=\"total\":year=\"2017\"", V1_45_4__migrate_thematic_maps.geIndicatorHash(indicator));
    }
}