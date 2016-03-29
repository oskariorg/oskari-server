package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.stats.VisualizationService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.axiom.om.OMElement;

import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * @deprecated GetStatsTile now supports SLD_BODY posting which makes this obsolete
 */
@Deprecated
@OskariActionRoute("GetStatsLayerSLD")
public class GetStatsLayerSLDHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetStatsLayerSLDHandler.class);
    final private VisualizationService service = new VisualizationService();

    // we need this
    final public static String PARAM_VISUALIZATION_ID = "visId";
    // OR these
    final public static String PARAM_VISUALIZATION_NAME = "name"; // name=ows:Kunnat2013
    final public static String PARAM_VISUALIZATION_FILTER_PROPERTY = "attr"; // attr=Kuntakoodi
    final public static String PARAM_VISUALIZATION_CLASSES = "classes"; // classes=020,091|186,086,982|111,139,740
    final public static String PARAM_VISUALIZATION_VIS = "vis"; // vis=choro:ccffcc|99cc99|669966

    final public static String PARAM_LANGUAGE = "lang";
    final public static String PARAM_LAYER_ID = "layerId";
    // mode is for debugging
    final private static String PARAM_MODE = "mode";
    final private static String MODE_XML = "XML";

    public void handleAction(final ActionParameters params)
            throws ActionException {

        final HttpServletResponse response = params.getResponse();
        response.setContentType("text/xml");
        ResponseHelper.writeResponse(params, getSLD(params));
    }

    private String getSLD(final ActionParameters params)
            throws ActionException {
    	
    	printParameters(params);

        final String lang = params.getHttpParam(PARAM_LANGUAGE, params
                .getLocale().getLanguage());

        final boolean modeXML = MODE_XML.equals(params.getHttpParam(PARAM_MODE,
                "").toUpperCase());

        final StatsVisualization vis = getVisualization(params);
        if (vis == null) {
            throw new ActionParamsException(
                    "Couldn't get requested visualization");
        }

        log.debug("Found visualization:", vis);
        final OMElement xml = service.getXML(vis, lang);
        try {
            if (modeXML) {
                return xml.toString();
            } else {
//                return service.transform(xml, service.getDefaultXSLT());
            	String xmlString = service.transform(xml, service.getDefaultXSLT());
            	
            	return xmlString;
            }
        } catch (Exception e) {
            throw new ActionException("Unable to create SLD", e);
        }
    }
    
    private void printParameters(ActionParameters params){
    	Enumeration e = params.getRequest().getParameterNames();
    	
    	log.debug("print keys");
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		log.debug("Key: " +key);
    		String[] values = params.getRequest().getParameterValues(key);
    		
    		for(String value : values){
    			log.debug("  value: " + value);
    		}
    	}
    }    

    /**
     * Constructs visualization parameters from request parameters
     *
     * @param params request parameters
     * @return Visualization parameters required to generate an SLD or empty/invalid StatsVisualization object if parameters were missing
     */
    private StatsVisualization getVisualization(final ActionParameters params) {
        final int visId = ConversionHelper.getInt(
                params.getHttpParam(PARAM_VISUALIZATION_ID), -1);
        final StatsVisualization vis = service.getVisualization(
                visId,
                params.getHttpParam(PARAM_VISUALIZATION_CLASSES),
                params.getHttpParam(PARAM_VISUALIZATION_NAME),
                params.getHttpParam(PARAM_VISUALIZATION_FILTER_PROPERTY),
                params.getHttpParam(PARAM_VISUALIZATION_VIS, "")
        );
        if (vis != null) {
            return vis;
        } else {
            return new StatsVisualization();
        }
    }

}