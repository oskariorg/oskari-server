package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

@OskariActionRoute("SearchOptions")
public class SearchOptionsHandler extends ActionHandler {

    private SearchService searchService;

    @Override
    public void init() {
        super.init();
        searchService = OskariComponentManager.getComponentOfType(SearchService.class);
    }
    public void handleAction(final ActionParameters params) throws ActionException {

        Map<String, SearchableChannel> channels =  searchService.getAvailableChannels();
        JSONArray channelsJSONArray = new JSONArray();
        for(SearchableChannel channel : channels.values()) {
            if(!channel.hasPermission(params.getUser()) || !channel.getCapabilities().canTextSearch()) {
                continue;
            }
            JSONObject json = new JSONObject();
            JSONHelper.putValue(json, "id", channel.getId());
            JSONHelper.putValue(json, "isDefault", channel.isDefaultChannel());
            JSONHelper.putValue(json, "locale", channel.getUILabels().optJSONObject(params.getLocale().getLanguage()));
            channelsJSONArray.put(json);
        }
        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, "channels", channelsJSONArray);

        try {
            ResponseHelper.writeResponse(params, response);
        } catch (Exception ex) {
            throw new ActionException("Couldn't get WFS search channels", ex);
        }
    }
}
