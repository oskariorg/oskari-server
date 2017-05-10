package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@OskariActionRoute("SearchOptions")
public class SearchOptionsHandler extends ActionHandler {

    private SearchService searchService;
    private Set<String> blacklist = new HashSet<>();

    @Override
    public void init() {
        super.init();
        searchService = OskariComponentManager.getComponentOfType(SearchService.class);
        String[] blacklistedChannelIDs = PropertyUtil.getCommaSeparatedList("actionhandler.SearchOptions.blacklist");
        if(blacklist.isEmpty()) {
            for(String id : blacklistedChannelIDs) {
                blacklist.add(id);
            }
        }
    }
    public void handleAction(final ActionParameters params) throws ActionException {

        Map<String, SearchableChannel> channels =  searchService.getAvailableChannels();
        JSONArray channelsJSONArray = new JSONArray();
        for(SearchableChannel channel : channels.values()) {
            if(!shouldBeIncluded(channel, params.getUser())) {
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

    protected boolean shouldBeIncluded(SearchableChannel channel, User user) {
        if(!channel.getCapabilities().canTextSearch()) {
            return false;
        }
        if(blacklist.contains(channel.getId())) {
            return false;
        }
        return channel.hasPermission(user);
    }
}
