package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
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
    private static final String PARAM_MAXFEATURES = "maxfeatures";
    private static final String PARAM_EPSG_KEY = "epsg";

    private static final String PROPERTY_CHANNELS = "actionhandler.GetReverseGeocodingResult.channels";
    private static final String PROPERTY_MAXFEATURES = "actionhandler.GetReverseGeocodingResult.maxfeatures";
    private static final String PROPERTY_BUFFER = "actionhandler.GetReverseGeocodingResult.buffer";

    private int maxFeatures = 1;
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

        final SearchCriteria sc = new SearchCriteria();
        sc.setReverseGeocode(ConversionHelper.getDouble(lat, -1), ConversionHelper.getDouble(lon, -1));
        // eg. EPSG:3067
        sc.setSRS(epsg);
        // max result feature count
        sc.setMaxResults(params.getHttpParam(PARAM_MAXFEATURES, maxFeatures));
        // Search distance around the point (unit m)
        sc.addParam(PARAM_BUFFER, params.getHttpParam(PARAM_BUFFER, buffer));
        sc.setLocale(params.getLocale().getLanguage());

        for (String channelId : channels) {
            sc.addChannel(channelId);
        }
        // TODO: enforce max features if there are multiple channels!
        final JSONObject result = SearchWorker.doSearch(sc);
        ResponseHelper.writeResponse(params, result);
    }

}
