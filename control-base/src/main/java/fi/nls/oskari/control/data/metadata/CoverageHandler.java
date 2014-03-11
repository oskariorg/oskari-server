package fi.nls.oskari.control.data.metadata;

import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ServiceFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 11.3.2014
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public class CoverageHandler extends MetadataFieldHandler {

    public JSONArray getOptions(final String language) {
        // CataloguePopulatorService.populateVillageSelects(locale)

        final JSONArray values = new JSONArray();
        return values;
    }
}
