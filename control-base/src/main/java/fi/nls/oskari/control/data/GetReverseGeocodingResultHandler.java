package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.util.*;

@OskariActionRoute("GetReverseGeocodingResult")
public class GetReverseGeocodingResultHandler extends ActionHandler {

    private static final String PARAM_LON = "lon";
    private static final String PARAM_LAT = "lat";
    private static final String PARAM_BUFFER = "buffer";
    private static final String PARAM_SCALE = "scale";
    private static final String PARAM_MAXFEATURES = "maxfeatures";
    private static final String PARAM_EPSG_KEY = "epsg";

    private static final String PROPERTY_CHANNELS = "actionhandler.GetReverseGeocodingResult.channels";
    private static final String PROPERTY_MAXFEATURES = "actionhandler.GetReverseGeocodingResult.maxfeatures";
    private static final String PROPERTY_BUFFER = "actionhandler.GetReverseGeocodingResult.buffer";
    // Reverse search is executed only to requested channels, if channel_ids parameter is used
    // TODO: refactor to channels to be consistent with GetSearchResultHandler
    private static final String PARAM_OPTIONAL_CHANNEL_IDS_KEY = "channel_ids";

    private int maxFeatures = -1;
    private int buffer = 1000;

    private String[] channels = new String[0];

    public void init() {
        channels = PropertyUtil.getCommaSeparatedList(PROPERTY_CHANNELS);
        maxFeatures = PropertyUtil.getOptional(PROPERTY_MAXFEATURES, maxFeatures);
        buffer = PropertyUtil.getOptional(PROPERTY_BUFFER, buffer);
        if(channels.length == 0) {
            SearchService searchService = new SearchServiceImpl();
            Map<String, SearchableChannel> availableChannels = searchService.getAvailableChannels();
            List<String> geocodeChannels = new ArrayList<>(availableChannels.size());
            for (Map.Entry<String, SearchableChannel> entry : availableChannels.entrySet()) {
                if(entry.getValue().getCapabilities().canGeocode()) {
                    geocodeChannels.add(entry.getKey());
                }
            }
            channels = geocodeChannels.toArray(new String[0]);
        }
    }


    public void handleAction(final ActionParameters params) throws ActionException {
        final String lon = params.getRequiredParam(PARAM_LON);
        final String lat = params.getRequiredParam(PARAM_LAT);
        final String epsg = params.getRequiredParam(PARAM_EPSG_KEY);
        final String scale = params.getHttpParam(PARAM_SCALE);

        final SearchCriteria sc = new SearchCriteria();
        
        if (scale != null) {
            sc.addParam(PARAM_SCALE, scale);
        }
        
        sc.setReverseGeocode(ConversionHelper.getDouble(lat, -1), ConversionHelper.getDouble(lon, -1));
        // eg. EPSG:3067
        sc.setSRS(epsg);

        // Search distance around the point (unit m)
        sc.addParam(PARAM_BUFFER, params.getHttpParam(PARAM_BUFFER, buffer));
        sc.setLocale(params.getLocale().getLanguage());

        // Requested channels. Option to use only e.g. one channel for to request the result
        List<String> requestedChannels = getChannels(params.getHttpParam(PARAM_OPTIONAL_CHANNEL_IDS_KEY));
        for (String channelId : requestedChannels) {
            sc.addChannel(channelId);
        }
        // determine max result feature count - in this order parameter value, property, number of channels to query
        sc.setMaxResults(getMaxResults(params.getHttpParam(PARAM_MAXFEATURES, maxFeatures), requestedChannels.size()));
        if (sc.getChannels().size() == 0) {
            throw new ActionParamsException("No reverse geocoding channels available or configured or invalid channel ID");
        }

        final JSONObject result = SearchWorker.doSearch(sc);
        ResponseHelper.writeResponse(params, result);
    }

    private int getMaxResults(int requested, int defaultValue) {
        if(requested != -1) {
            return requested;
        }
        return defaultValue;
    }

    private List<String> getChannels(final String requested) {
        if (requested != null) {
            return Arrays.asList(requested.split(","));
        }
        return Arrays.asList(channels);
    }

}
