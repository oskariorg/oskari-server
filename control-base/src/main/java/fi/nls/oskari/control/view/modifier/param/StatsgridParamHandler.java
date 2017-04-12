package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("statsgrid")
public class StatsgridParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(StatsgridParamHandler.class);
    private static final String PARAM_LAYERID = "layerId";
    private static final String PARAM_CURRENTCOLUMN = "currentColumn";
    private static final String PARAM_METHODID = "methodId";
    private static final String PARAM_MANUALBREAKSINPUT = "manualBreaksInput";
    private static final String PARAM_NUMBEROFCLASSES = "numberOfClasses";
    private static final String PARAM_VERSION = "version";
    private static final String PARAM_CLASSIFICATIONMODE = "classificationMode";
    private static final String PARAM_ISACTIVE = "isActive";

    private static final String PARAM_INDICATORS = "indicators";
    private static final String KEY_INDICATOR = "indicator";
    private static final String KEY_YEAR = "year";
    private static final String KEY_GENDER = "gender";

    private static final String PARAM_COLORS = "colors";
    private static final String KEY_COLORSET = "set";
    private static final String KEY_COLORINDEX = "index";
    private static final String KEY_COLORFLIPPED = "flipped";

    private boolean parseStateV1(final ModifierParams params,
            String[] values, String[] parts, String[] indicators, JSONObject statsgridState) throws ModifierException {
        
        // Note: Do not change the parsing in this method, because it parses URLs created by the old version
        //       of the application.
        
        // parse indicators
        final JSONArray indicatorsJson = new JSONArray();
        for (String indicatorString : indicators) {
            final String[] indicatorProps = indicatorString.split(" ");
            final JSONObject indicator = getIndicatorJsonV1(indicatorProps, params.getReferer());
            if(indicator != null) {
                indicatorsJson.put(indicator);
            }
        }

        // parse values
        if (values.length < 4) {
            // We need all known values, if not provided we bail
            // manualBreaksInput is optional, but we only use the first 5
            return false;
        }

        // parse required values
        final String layerId = values[0];
        final String currentColumn = values[1];
        final String methodId = values[2];
        final String numberOfClasses = values[3];

        log.info("parts length: " + parts.length);
        try {
            statsgridState.put(PARAM_LAYERID, layerId);
            statsgridState.put(PARAM_CURRENTCOLUMN, currentColumn);
            statsgridState.put(PARAM_METHODID, methodId);
            statsgridState.put(PARAM_NUMBEROFCLASSES, numberOfClasses);
            statsgridState.put(PARAM_VERSION, "1");
            if (values.length == 5) {
                final String classificationMode = values[4];
                statsgridState.put(PARAM_CLASSIFICATIONMODE, classificationMode);
            }
            if (values.length == 6) {
                // parse optional parameters 
                final String manualBreaksInput = values[5];
                statsgridState.put(PARAM_MANUALBREAKSINPUT, manualBreaksInput);
            }
            if (parts.length >= 3 && parts[2].length() > 0) {
                final String[] colors = parts[2].split(",");
                JSONObject colorsJson = getColorsJson(colors);
                if (colorsJson != null) {
                    statsgridState.put(PARAM_COLORS, colorsJson);
                }
            }
            if (parts.length >= 4) {
                final boolean isActive = "1".equals(parts[3]);
                statsgridState.put(PARAM_ISACTIVE, isActive);
            }
            statsgridState.put(PARAM_INDICATORS, indicatorsJson);
        } catch (JSONException je) {
            throw new ModifierException("Could not replace statsgrid state!");
        }
        return false;
    }

    private boolean parseStateV2(final ModifierParams params,
            String[] values, String[] parts, String[] indicators, JSONObject statsgridState) throws ModifierException {
        // parse indicators
        final JSONArray indicatorsJson = new JSONArray();
        for (String indicatorString : indicators) {
            final JSONObject indicator = getIndicatorJsonV2(indicatorString, params.getReferer());
            if(indicator != null) {
                indicatorsJson.put(indicator);
            }
        }

        // parse values
        if (values.length < 4) {
            // We need all known values, if not provided we bail
            // manualBreaksInput is optional, but we only use the first 5
            return false;
        }
        // Example: 11+0+1+4+0+0+0+2
        // parse required values
        final String layerId = values[0];
        final String currentColumn = values[1];
        final String methodId = values[2];
        final String numberOfClasses = values[3];
        final String classificationMode = values[4];
        final String manualBreaksInput = values[5];
        final String version = values[7];

        log.info("parts length: " + parts.length);
        try {
            statsgridState.put(PARAM_LAYERID, layerId);
            statsgridState.put(PARAM_CURRENTCOLUMN, currentColumn);
            statsgridState.put(PARAM_METHODID, methodId);
            statsgridState.put(PARAM_NUMBEROFCLASSES, numberOfClasses);
            statsgridState.put(PARAM_VERSION, version);
            statsgridState.put(PARAM_CLASSIFICATIONMODE, classificationMode);
            statsgridState.put(PARAM_MANUALBREAKSINPUT, manualBreaksInput);

            // The last two parts, colors and isActive, are optional.
            if (parts.length >= 3 && parts[2].length() > 0) {
                final String[] colors = parts[2].split(",");
                JSONObject colorsJson = getColorsJson(colors);
                if (colorsJson != null) {
                    statsgridState.put(PARAM_COLORS, colorsJson);
                }
            }
            // This is never put into the URL if the colors part is not put in also (possibly empty).
            if (parts.length >= 4) {
                final boolean isActive = "1".equals(parts[3]);
                statsgridState.put(PARAM_ISACTIVE, isActive);
            }
            statsgridState.put(PARAM_INDICATORS, indicatorsJson);
        } catch (JSONException je) {
            throw new ModifierException("Could not replace statsgrid state!");
        }
        return false;
    }
    
    public boolean handleParam(final ModifierParams params) throws ModifierException {
    	final String paramValues = params.getParamValue();
        if(paramValues == null) {
            return false;
        }
        log.debug("StatsgridParamHandler got:", paramValues, "\n");
        // TODO: There is really no reason to transport all these through the backend.
        //       The client is perfectly capable of reading its state from the query parameters or path in the URL.

    	final String[] parts = paramValues.split("-");

    	if (parts.length < 2) {
    		// We need both value and indicator list, if not provided we bail
            // optional colors part is the third one
    		return false;
    	}
    	// parse values and indicators
    	// Note: The + in the URL are changed into spaces here.
    	// The values in the version one were:
        /*
         * 'layerId', 'currentColumn', 'methodId', 'numberOfClasses', 'classificationMode',
         * 'manualBreaksInput', 'allowClassification'
         */
    	// The new values are:
    	/*
    	 * 'layerId', 'currentColumn', 'methodId', 'numberOfClasses', 'classificationMode',
    	 * 'manualBreaksInput', 'allowClassification', 'version'
    	 */
    	final String[] values = parts[0].split(" ");
    	// If values.length <= 7, this is version 1.
    	// Otherwise, if the values.length > 7, this is version values[7].
    	// Example v1 URL: http://www.paikkatietoikkuna.fi/web/fi/kartta?zoomLevel=0&coord=520000_7250000&mapLayers=519+100+&statsgrid=519+indicator42014total+1+5+++-4+2014+total-seq,0,false-1&showMarker=true
    	// Example v2 URL: http://www.paikkatietoikkuna.fi/web/fi/kartta?zoomLevel=0&coord=520000_7250000&mapLayers=11+100+&statsgrid=11+0+1+4+0+0+0+2-fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin:167:11:{"sex":"male","year":"1994"}+fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin:179:11:{"year":"1997"}-seq,0,false-1&showMarker=true

    	String version = "1";
    	if (values.length > 7) {
    	    version = values[7];
    	}
        final String[] indicators = parts[1].split(" ");
        final JSONObject statsgridState = getBundleState(params.getConfig(), BUNDLE_STATSGRID);

        if (version.equals("1")) {
            return parseStateV1(params, values, parts, indicators, statsgridState);
        } else if (version.equals("2")) {
            return parseStateV2(params, values, parts, indicators, statsgridState);
        } else {
            log.error("Illegal version: " + version);
        }
        return false;
    }

    public static JSONObject getColorsJson(final String[] colors) {
        if (colors.length == 3) {
            final JSONObject colorsJson = new JSONObject();
            final String colorSet = colors[0];
            final int colorIndex = ConversionHelper.getInt(colors[1], 0);
            final Boolean colorsFlipped = ConversionHelper.getBoolean(colors[2], false);

            try {
                colorsJson.put(KEY_COLORSET, colorSet);
                colorsJson.put(KEY_COLORINDEX, colorIndex);
                colorsJson.put(KEY_COLORFLIPPED, colorsFlipped);

                return colorsJson;
            } catch (JSONException je) {
                log.warn("Could not create colors JSON from params:", colors);
                return null;
            }
        } else {
            return null;
        }
    }
    
    public static JSONObject getIndicatorJsonV1(final String[] indicatorParam, final String referer) throws ModifierException {

		// Indicators consists of 3 parts, the indicator (id), year, and gender.
    	// All parts are needed to reliably identify indicators.
        if (indicatorParam.length != 2) {
            final String indicator = indicatorParam[0];
            final String year = indicatorParam[1];
            final String gender = indicatorParam[2];

            final JSONObject indicatorJson = new JSONObject();
            try {
            	indicatorJson.put(KEY_INDICATOR, indicator);
            	indicatorJson.put(KEY_YEAR, year);
            	indicatorJson.put(KEY_GENDER, gender);
                return indicatorJson;
            } catch (JSONException je) {
                log.warn("Could not create layer JSON from params:", indicatorParam);
                throw new ModifierException("Could not populate layer JSON");
            }
    	} else {
        	return null;
        }
    }
    
    public static JSONObject getIndicatorJsonV2(final String indicatorParam, final String referer) throws ModifierException {
        // Example indicator id:
        // fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin:167:11:{"sex":"female","year":"1991"}"
        // This is the "cacheKey" used by the frontend: datasourceId + ":" + indicatorId + ":" + selectedLayer + ":" + selectorsString
        
        final JSONObject indicatorJson = new JSONObject();
        try {
            indicatorJson.put(KEY_INDICATOR, indicatorParam);
            return indicatorJson;
        } catch (JSONException je) {
            log.warn("Could not create layer JSON from params:", indicatorParam);
            throw new ModifierException("Could not populate layer JSON");
        }
    }
}
