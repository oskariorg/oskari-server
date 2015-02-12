package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.util.Locale;

@OskariActionRoute("GetReverseGeocodingResult")
public class GetReverseGeocodingResultHandler extends ActionHandler {

    private static final String PARAM_LON = "lon";
    private static final String PARAM_LAT = "lat";
    private static final String PARAM_BUFFER = "buffer";
    private static final String PARAM_MAXFEATURES = "maxfeatures";
    private static final String PARAM_EPSG_KEY = "epsg";

    private static final String KEY_CHANNELS = "actionhandler.GetReverseGeocodingResult.channels";
    private static final String KEY_PREFIX = "search.channel.";
    private static final String KEY_MAXFEATURES = "service.maxfeatures";
    private static final String KEY_BUFFER = "service.buffer";

    private static final String defaultMaxFeatures = "1";
    private static final String defaultBuffer = "1000";
    private static String maxFeatures = "1";
    private static String buffer = "1000";

    private String[] channels = new String[0];

    public void init() {
        channels = PropertyUtil.getCommaSeparatedList(KEY_CHANNELS);
        if(channels.length > 0) {
            maxFeatures = PropertyUtil.get(KEY_PREFIX + channels[0] + KEY_MAXFEATURES, defaultMaxFeatures);
            buffer = PropertyUtil.get(KEY_PREFIX + channels[0] +KEY_BUFFER, defaultBuffer);
        }
    }


    public void handleAction(final ActionParameters params) throws ActionException {
        final String lon = params.getRequiredParam(PARAM_LON);
        final String lat = params.getRequiredParam(PARAM_LAT);
        final String epsg = params.getRequiredParam(PARAM_EPSG_KEY);
        final Locale locale = params.getLocale();
        final SearchCriteria sc = new SearchCriteria();
        // No search string in reverse geocoding
        sc.setSearchString("");
        // eg. EPSG:3067
        sc.setSRS(epsg);
        // Search distance around the point (unit m)
        sc.addParam(PARAM_BUFFER, params.getHttpParam(PARAM_BUFFER, buffer));
        // max result feature count
        sc.addParam(PARAM_MAXFEATURES, params.getHttpParam(PARAM_MAXFEATURES, maxFeatures));
        sc.addParam(PARAM_LON, lon);
        sc.addParam(PARAM_LAT, lat);

        sc.setLocale(locale.getLanguage());

        for (String channelId : channels) {
            sc.addChannel(channelId);
        }
        final JSONObject result = SearchWorker.doSearch(sc);
        ResponseHelper.writeResponse(params, result);
    }

}
