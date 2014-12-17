package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

/**
 * Note this lists search channels that are annotated and found in the classpath.
 * It doesn't show which channels are actually used by search.
 * To get the actually used search channels we would need to get reference to
 * the search service being used.
 */
@OskariActionRoute("SearchChannel")
public class SearchChannelHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(SearchChannelHandler.class);

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final JSONObject response = new JSONObject();
        final JSONArray list = new JSONArray();
        final Map<String, SearchChannel> annotatedChannels = OskariComponentManager.getComponentsOfType(SearchChannel.class);
        for (Map.Entry<String, SearchChannel> entry : annotatedChannels.entrySet()) {
            final JSONObject json = new JSONObject();
            final SearchChannel channel = entry.getValue();
            JSONHelper.putValue(json, "name", entry.getKey());
            final Map<String, Object> data = channel.getDebugData();
            for(String key : data.keySet()) {
                JSONHelper.putValue(json, key, data.get(key));
            }
            list.put(json);
        }
        JSONHelper.putValue(response, "channels", list);
        JSONHelper.putValue(response, "timestamp", new Date());
        ResponseHelper.writeResponse(params, response);
    }


    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}