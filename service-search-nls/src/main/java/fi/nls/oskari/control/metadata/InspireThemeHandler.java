package fi.nls.oskari.control.metadata;

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
public class InspireThemeHandler extends MetadataFieldHandler {

    private static InspireThemeService inspireThemeService = ServiceFactory.getInspireThemeService();

    public JSONArray getOptions(final String language) {
        final List<InspireTheme> themes = inspireThemeService.findAll();

        final JSONArray values = new JSONArray();
        for(InspireTheme theme : themes) {
            final JSONObject value = JSONHelper.createJSONObject("val", theme.getId());
            JSONHelper.putValue(value, "locale", theme.getName(language));
            values.put(value);
        }
        return values;
    }
}
