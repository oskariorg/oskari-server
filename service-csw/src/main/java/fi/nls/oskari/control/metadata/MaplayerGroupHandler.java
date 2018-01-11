package fi.nls.oskari.control.metadata;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
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
public class MaplayerGroupHandler extends MetadataFieldHandler {

    private static OskariMapLayerGroupService oskariMapLayerGroupService = OskariComponentManager.getComponentOfType(OskariMapLayerGroupService.class);

    public void handleParam(final String param, final SearchCriteria criteria) {
        final int groupId = ConversionHelper.getInt(param, -1);
        if(groupId != -1) {
            MaplayerGroup theme = oskariMapLayerGroupService.find(groupId);
            criteria.addParam(getPropertyName(), theme.getName(criteria.getLocale()));
        }
    }

    public JSONArray getOptions(final String language) {
        final List<MaplayerGroup> themes = oskariMapLayerGroupService.findAll();

        final JSONArray values = new JSONArray();
        for(MaplayerGroup theme : themes) {
            final JSONObject value = JSONHelper.createJSONObject("val", theme.getId());
            JSONHelper.putValue(value, "locale", theme.getName(language));
            values.put(value);
        }
        return values;
    }
}
