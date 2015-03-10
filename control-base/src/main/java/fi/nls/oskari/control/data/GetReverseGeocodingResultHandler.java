package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
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

    private static final String PROPERTY_CHANNELS = "actionhandler.GetReverseGeocodingResult.channels";
    private static final String PROPERTY_MAXFEATURES = "actionhandler.GetReverseGeocodingResult.maxfeatures";
    private static final String PROPERTY_BUFFER = "actionhandler.GetReverseGeocodingResult.buffer";

    private static int maxFeatures = 1;
    private static int buffer = 1000;

    private String[] channels = new String[0];

    public void init() {
        channels = PropertyUtil.getCommaSeparatedList(PROPERTY_CHANNELS);
        maxFeatures = PropertyUtil.getOptional(PROPERTY_MAXFEATURES, maxFeatures);
        buffer = PropertyUtil.getOptional(PROPERTY_BUFFER, buffer);
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
        // TODO: enforce max features if there are multiple channels!
        final JSONObject result = SearchWorker.doSearch(sc);
        ResponseHelper.writeResponse(params, result);
    }

}
