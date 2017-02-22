package fi.nls.oskari.control.view.modifier.param;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONObject;

@OskariViewModifier("what3words")
public class What3wordsParamHandler extends ParamHandler {

    @Override
    // CoordinateParamHandler has higher priority number and is executed after this
    public int getPriority() {
        return 9;
    }

    public static final String ID_WHAT3WORDS_CHANNEL = "WHAT3WORDS_CHANNEL";
    public static final String KEY_LOCATIONS = "locations";
    public static final String KEY_LON = "lon";
    public static final String KEY_LAT = "lat";
    public static final String KEY_SRS = "srs";
    private static final Logger log = LogFactory.getLogger(What3wordsParamHandler.class);

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if (params.getParamValue() == null) {
            return false;
        }

        try {
            final JSONObject state = getBundleState(params.getConfig(), ViewModifier.BUNDLE_MAPFULL);
            String w3w = params.getParamValue();
            final SearchCriteria sc = new SearchCriteria();
            sc.setSearchString(w3w);
            sc.setSRS(JSONHelper.getStringFromJSON(state, KEY_SRS, "EPSG:3067"));
            sc.addChannel(ID_WHAT3WORDS_CHANNEL);
            final JSONObject result = SearchWorker.doSearch(sc);

            if(result != null) {
                final String[] coords = parseParam(result);
                if (coords.length == 2) {
                    state.put(ViewModifier.KEY_EAST, coords[0]);
                    state.put(ViewModifier.KEY_NORTH, coords[1]);
                    return true;
                }
                return false;

            }

        } catch (Exception je) {
            throw new ModifierException("Could not set what3words from URL param.");
        }
        return false;
    }

    /**
     * Parse coordinates out of w3w search result
     * @param jsresult search result json
     * @return
     */
    public static String[] parseParam(JSONObject jsresult) {
        String[] coords = new String[0];
        JSONObject location1st = null;
        if (jsresult.has("locations")) {
            location1st = JSONHelper.getJSONObject(JSONHelper.getJSONArray(jsresult, KEY_LOCATIONS), 0);
        }
        if (location1st != null) {
            coords = new String[2];
            coords[0] = JSONHelper.getStringFromJSON(location1st, KEY_LON, null);
            coords[1] = JSONHelper.getStringFromJSON(location1st, KEY_LAT, null);
        }
        return coords;
    }
}
