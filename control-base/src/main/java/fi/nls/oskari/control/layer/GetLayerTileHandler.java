package fi.nls.oskari.control.layer;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Enumeration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import fi.nls.oskari.domain.map.OskariLayer;
import org.json.JSONArray;
import org.json.JSONObject;

import static fi.nls.oskari.control.ActionConstants.*;

@OskariActionRoute("GetLayerTile")
public class GetLayerTileHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetLayerTileHandler.class);
    private static final String LEGEND = "legend";
    private static final String NAME = "name";
    private static final List<String> RESERVED_PARAMETERS = Arrays.asList(new String[] {KEY_ID, ActionControl.PARAM_ROUTE, LEGEND});
    private static final int TIMEOUT_CONNECTION = PropertyUtil.getOptional("GetLayerTile.timeout.connection", 1000);
    private static final int TIMEOUT_READ = PropertyUtil.getOptional("GetLayerTile.timeout.read", 5000);
    private static final boolean GATHER_METRICS = PropertyUtil.getOptional("GetLayerTile.metrics", true);
    private static final String METRICS_PREFIX = "Oskari.GetLayerTile";
    private static final String ORG_STYLES = "org_styles";
    private PermissionHelper permissionHelper;
    private static final LayerJSONFormatterWMS FORMATTER = new LayerJSONFormatterWMS();

    /**
     *  Init method
     */
    public void init() {
        permissionHelper = new PermissionHelper(ServiceFactory.getMapLayerService(),ServiceFactory.getPermissionsService());
    }

    /**
     * Action handler
     * @param params Parameters
     * @throws ActionException
     */
    public void handleAction(final ActionParameters params)
            throws ActionException {

        // Resolve layer
        final String layerId = params.getRequiredParam(KEY_ID);
        final OskariLayer layer = permissionHelper.getLayer(layerId, params.getUser());

        final MetricRegistry metrics = ActionControl.getMetrics();

        Timer.Context actionTimer = null;
        if(GATHER_METRICS) {
            final com.codahale.metrics.Timer timer = metrics.timer(METRICS_PREFIX + "." + layerId);
            actionTimer = timer.time();
        }
        // Create connection
        final String url = getURL(params, layer);
        if(url == null || url.isEmpty()) {
            ResponseHelper.writeError(params, "Not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final HttpURLConnection con = getConnection(url, layer);
        try {
            con.setRequestMethod("GET");
            con.setDoOutput(false);
            con.setConnectTimeout(TIMEOUT_CONNECTION);
            con.setReadTimeout(TIMEOUT_READ);
            con.setDoInput(true);
            con.setFollowRedirects(true);
            con.setUseCaches(false);
            con.connect();

            final int responseCode = con.getResponseCode();
            final String contentType = con.getContentType();
            if(responseCode != HttpURLConnection.HTTP_OK || !contentType.startsWith("image/")) {
                LOG.warn("URL", url, "returned HTTP response code", responseCode,
                        "with message", con.getResponseMessage(), "and content-type:", contentType);
                String msg = IOHelper.readString(con);
                LOG.info("Response was:", msg);
                throw new ActionParamsException("Problematic response from actual service");
            }

            // read the image tile
            final byte[] presponse = IOHelper.readBytes(con);
            final HttpServletResponse response = params.getResponse();
            response.setContentType(contentType);
            response.getOutputStream().write(presponse, 0, presponse.length);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch(ActionException e) {
            // just throw it as is if we already handled it
            throw e;
        } catch (Exception e) {
            throw new ActionParamsException("Couldn't proxy request to actual service", e.getMessage(), e);
        } finally {
            if(actionTimer != null) {
                actionTimer.stop();
            }
            if(con != null) {
                con.disconnect();
            }
        }
    }


    private String getURL(final ActionParameters params, final OskariLayer layer) {
        if (params.getHttpParam(LEGEND, false)) {
            return this.getLegendURL(layer, params.getHttpParam(LayerJSONFormatterWMS.KEY_STYLE, null));
        }
        final HttpServletRequest httpRequest = params.getRequest();
        Enumeration<String> paramNames = httpRequest.getParameterNames();
        Map<String, String> urlParams = new HashMap<>();
        // Refine parameters
        while (paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            if (!RESERVED_PARAMETERS.contains(paramName)) {
                urlParams.put(paramName, params.getHttpParam(paramName));
            }
        }
        return IOHelper.constructUrl(layer.getUrl(),urlParams);
    }

    /**
     * Get Legend image url
     * @param layer  Oskari layer
     * @param style_name  style name for legend
     * @return
     */
    private String getLegendURL(final OskariLayer layer, String style_name) {
        String lurl = layer.getLegendImage();
        if (style_name != null) {
            // Get Capabilities style url
            JSONObject json = FORMATTER.getJSON(layer, PropertyUtil.getDefaultLanguage(), false);
            if (json.has(ORG_STYLES)) {

                JSONArray styles = JSONHelper.getJSONArray(json, ORG_STYLES);
                for (int i = 0; i < styles.length(); i++) {
                    final JSONObject style = JSONHelper.getJSONObject(styles, i);
                    if (JSONHelper.getStringFromJSON(style, NAME, "").equals(style_name)) {
                        return style.optString(LEGEND);
                    }
                }

            }
        }
        return lurl;

    }
    /**
     * Creates connection
     * @param url URL (with params) to call
     * @param layer layer
     * @return connection
     * @throws ActionException
     */
    private HttpURLConnection getConnection(final String url, final OskariLayer layer)
            throws ActionException {
        try {
            final String username = layer.getUsername();
            final String password = layer.getPassword();
            LOG.debug("Getting layer tile from url:", url);
            return IOHelper.getConnection(url, username, password);
        } catch (Exception e) {
            throw new ActionException("Couldn't get connection to service", e);
        }
    }
}
