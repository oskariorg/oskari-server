package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.stats.VisualizationService;
import fi.nls.oskari.map.stats.VisualizationServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

@OskariActionRoute("GetStatsTile")
public class GetStatsTileHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetStatsTileHandler.class);
    final private static String PARAM_LAYER_ID = "LAYERID";

    private final VisualizationService service = new VisualizationServiceIbatisImpl();

    final private static String PARAM_VISUALIZATION_ID = "VIS_ID";
    final private static String PARAM_VISUALIZATION_NAME = "VIS_NAME"; // name=ows:Kunnat2013
    final private static String PARAM_VISUALIZATION_FILTER_PROPERTY = "VIS_ATTR"; // attr=Kuntakoodi
    final private static String PARAM_VISUALIZATION_CLASSES = "VIS_CLASSES"; // classes=020,091|186,086,982|111,139,740
    final private static String PARAM_VISUALIZATION_VIS = "VIS_COLORS"; // vis=choro:ccffcc|99cc99|669966

    public void handleAction(final ActionParameters params)
            throws ActionException {

        final HttpURLConnection con = getConnection(params);
        try {
            // we should post complete GetMap XML with the custom SLD to geoserver so it doesn't need to fetch it again
            // Check: http://geo-solutions.blogspot.fi/2012/04/dynamic-wms-styling-with-geoserver-sld.html
            con.setRequestMethod("GET");

            con.setDoOutput(false);
            con.setDoInput(true);
            HttpURLConnection.setFollowRedirects(false);
            con.setUseCaches(false);
            con.connect();
            //IOHelper.writeToConnection(con, SLD_HANDLER.getSLD(params));

            // read the image tile
            final byte[] presponse = IOHelper.readBytes(con.getInputStream());

            final HttpServletResponse response = params.getResponse();
            response.setContentType("image/png");
            response.getOutputStream().write(presponse, 0, presponse.length);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request to geoserver",
                    e);
        } finally {
            if(con != null) {
                con.disconnect();
            }
        }
    }
    
    private HttpURLConnection getConnection(final ActionParameters params)
            throws ActionException {

        // copy parameters
        final HttpServletRequest httpRequest = params.getRequest();
        final StringBuilder ajaxUrl = new StringBuilder(PropertyUtil.get("statistics.sld.server"));
        ajaxUrl.append(PropertyUtil.get("statistics.sld.server.path",
                PropertyUtil.get(params.getLocale(), GetAppSetupHandler.PROPERTY_AJAXURL)));
        ajaxUrl.append("&action_route=GetStatsLayerSLD");
        final String layerId = params.getHttpParam(PARAM_LAYER_ID);
        
        if(layerId == null) {
            throw new ActionParamsException("Layer not specified: LAYERID-parameter missing");
        }
        ajaxUrl.append("&");
        ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_LAYER_ID);
        ajaxUrl.append("=");
        ajaxUrl.append(layerId);

        StatsVisualization vis = getVisualization(params);
        if(vis == null) {
            log.info("Visualization couldn't be generated - parameters/db data missing", params);
        } else {
            // using prefetched values so we don't need to get them from db again on SLD action
            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_NAME);
            ajaxUrl.append("=");
            ajaxUrl.append(vis.getLayername());

            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_FILTER_PROPERTY);
            ajaxUrl.append("=");
            ajaxUrl.append(vis.getFilterproperty());

            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_CLASSES);
            ajaxUrl.append("=");
            ajaxUrl.append(vis.getClasses());

            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_VIS);
            ajaxUrl.append("=");

            ajaxUrl.append(vis.getVisualization());
            ajaxUrl.append(":");
            ajaxUrl.append(vis.getColors());
        }
                
        final StringBuffer queryString = new StringBuffer();
        for (Object key : httpRequest.getParameterMap().keySet()) {
            String keyStr = (String) key;
            queryString.append("&");
            queryString.append(keyStr);
            queryString.append("=");
            queryString.append(params.getHttpParam(keyStr));
        }
        try {
            final String url = PropertyUtil.get("statistics.geoserver.wms.url") + queryString + "&SLD=" + URLEncoder.encode(ajaxUrl.toString(), "UTF-8");
            log.debug("Getting stats tile from url:", url);
            return IOHelper.getConnection(url, PropertyUtil.get("statistics.user"), PropertyUtil.get("statistics.password"));
        } catch (Exception e) {
            throw new ActionException(
                    "Couldnt get connection to geoserver", e);
        }
    }

    private StatsVisualization getVisualization(final ActionParameters params) {
        final int statsLayerId = ConversionHelper.getInt(
                params.getHttpParam(PARAM_LAYER_ID), -1);
        final int visId = ConversionHelper.getInt(
                params.getHttpParam(PARAM_VISUALIZATION_ID), -1);
        return service.getVisualization(
                statsLayerId, 
                visId,
                params.getHttpParam(PARAM_VISUALIZATION_CLASSES),
                params.getHttpParam(PARAM_VISUALIZATION_NAME),
                params.getHttpParam(PARAM_VISUALIZATION_FILTER_PROPERTY),
                params.getHttpParam(PARAM_VISUALIZATION_VIS, "")
                );
        
    }

}