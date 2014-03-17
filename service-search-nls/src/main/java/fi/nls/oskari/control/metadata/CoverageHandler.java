package fi.nls.oskari.control.metadata;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.village.Village;
import fi.nls.oskari.search.village.VillageService;
import fi.nls.oskari.search.village.VillageServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 11.3.2014
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public class CoverageHandler extends MetadataFieldHandler {

    private final Logger log = LogFactory.getLogger(CoverageHandler.class);

    // the table for villages is in another DB
    // FIXME: bring the table with contents to common DB or expose as a service
    private static VillageService villageService = new VillageServiceIbatisImpl() {
        @Override
        protected String getSqlMapLocation() {
            return "/META-INF/SqlMapConfigMMLPool.xml";
        }
    };

    public JSONArray getOptions(final String language) {
        final JSONArray values = new JSONArray();
        try {
            for (Village village : villageService.findAll()) {
                final JSONObject value = JSONHelper.createJSONObject("val", village.getWgs84wkt());
                JSONHelper.putValue(value, "locale", village.getName(language));
                values.put(value);
            }
        } catch(Exception ex) {
            log.error("Couldn't get villages for coverage options:", log.getCauseMessages(ex));
        }
        return values;
    }
}
