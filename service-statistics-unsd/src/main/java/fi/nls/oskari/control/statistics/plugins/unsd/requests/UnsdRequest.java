package fi.nls.oskari.control.statistics.plugins.unsd.requests;

import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.unsd.UnsdConfig;
import fi.nls.oskari.util.IOHelper;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Base class for UNSD statistics queries.
 */
public class UnsdRequest {

    private static final String PLACEHOLDER_GOAL = "{goalCode}";
    private static final String PARAM_INDICATOR = "indicator";
    private static final String PARAM_PAGE = "page";
    private static final String PATH_GOAL = "Goal/" + PLACEHOLDER_GOAL;
    private static final String PATH_TARGETS = PATH_GOAL + "/Target/List";
    private static final String PATH_INDICATOR_DATA = "Indicator/Data/";
    private static final String PATH_DIMENSIONS = PATH_GOAL + "/Dimensions";

    private UnsdConfig config;
    private String goal;
    private String indicator;
    private Integer page;
    private String[] areaCodes;

    public UnsdRequest(UnsdConfig config) {
        this.config = config;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public void nextPage() {
        if (page == null) {
            page = 1;
        }
        page++;
    }

    public void setAreaCodes(String[] areaCodes) {
        this.areaCodes = areaCodes;
    }

    /**
     * To request list of goal targets and their indicators
     *
     * @return json response
     * model:
     * [{
     *   "code": "string",
     *   "title": "string",
     *   "description": "string",
     *   "uri": "string",
     *   "targets": [
     *     {
     *       "goal": "string",
     *       "code": "string",
     *       "title": "string",
     *       "description": "string",
     *       "uri": "string",
     *       "indicators": [
     *         {
     *           "goal": "string",
     *           "target": "string",
     *           "code": "string",
     *           "description": "string",
     *           "tier": "string",
     *           "uri": "string",
     *           "series": [
     *             {
     *               "goal": [
     *                 "string"
     *               ],
     *               "target": [
     *                 "string"
     *               ],
     *               "indicator": [
     *                 "string"
     *               ],
     *               "release": "string",
     *               "code": "string",
     *               "description": "string",
     *               "uri": "string"
     *             }
     *           ]
     *         }
     *       ]
     *     }
     *   ]
     * }]
     */
    public String getTargets() {
        Map<String, String> params = Collections.singletonMap("includechildren", "true");
        return getData(PATH_TARGETS.replace(PLACEHOLDER_GOAL, goal), params);
    }

    /**
     * To get goal dimensions
     * @return json response
     * model:
     * [{
     *     "id": "string",
     *     "codes": [{
     *         "code": "string",
     *             "description": "string",
     *             "sdmx": "string"
     *     }]
     * }]
     */
    public String getDimensions() {
        return getData(PATH_DIMENSIONS.replace(PLACEHOLDER_GOAL, goal), Collections.EMPTY_MAP);
    }

    public String getIndicatorData(StatisticalIndicatorDataModel selectors) {
        final Map<String, String> params = new HashMap<>();
        params.put(PARAM_INDICATOR, indicator);
        if (page != null) {
            params.put(PARAM_PAGE, page.toString());
        }
        if (selectors != null) {
            selectors.getDimensions().stream().forEach(
                    dimension -> params.put(dimension.getId(), dimension.getValue()));
        }
        return getData(PATH_INDICATOR_DATA, params);
    }

    private String getUrl(String path, Map<String, String> params) {
        String url = IOHelper.constructUrl(config.getUrl() + "/" + path, params);
        if (areaCodes != null && areaCodes.length > 0) {
            return IOHelper.addUrlParam(url, "areaCode", areaCodes);
        }
        return url;
    }

    private String getData(String path, Map<String, String> params) throws APIException {
        HttpURLConnection con = null;
        try {
            final String url = getUrl(path, params);
            con = IOHelper.getConnection(url);
            return IOHelper.readString(con.getInputStream());
        } catch (Exception e) {
            throw new APIException("Couldn't request data from the UNSD server", e);
        } finally {
            try {
                con.disconnect();
            }
            catch (Exception ignored) {}
        }
    }
}
