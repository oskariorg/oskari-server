package fi.nls.oskari.control.wfs;

import javax.servlet.http.HttpServletRequest;

import fi.mml.portti.service.ogc.executor.WfsExecutorService;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import fi.mml.portti.service.ogc.OgcFlowDefinitions;
import fi.mml.portti.service.ogc.OgcFlowException;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.Logger;

public abstract class FlowModelHandler extends ActionHandler {

    private final static Logger log = LogFactory.getLogger(FlowModelHandler.class);

    private static final String PROPERTY_THREAD_COUNT = "wfsexecutorservice.threadcount";
    private static boolean threadsInitialized = false;
    
    protected static final String KEY_ACTION = "action_route";
    protected static final String KEY_IMAGE = "image";
    protected static final String KEY_XMLDATA = "xmlData";

    public void init() {
        super.init();
        if(!threadsInitialized) {
            threadsInitialized = true;
            final int wfsThreadCount = ConversionHelper.getInt(PropertyUtil.get(PROPERTY_THREAD_COUNT), 5);
            WfsExecutorService.start(wfsThreadCount);
        }
    }

    @Override
    public void teardown() {
        // cleanup WFS thread pool
        WfsExecutorService.shutDown();
        threadsInitialized = false;

        super.teardown();
    }

    /**
     * Creates a flow model that contains all request parameters.
     * 
     * @param params
     * @return flow model
     */
    protected static FlowModel createFlowModelFlowModel(ActionParameters params) {

        HttpServletRequest request = params.getRequest();
        
        FlowModel fm = new FlowModel();
        fm.setUser(params.getUser());

        for (Object o : request.getParameterMap().keySet()) {
            String key = (String) o;
            key = Jsoup.clean(key, Whitelist.none());
            String value = request.getParameter(key);
            value = Jsoup.clean(value, Whitelist.none());
            fm.put(key, value);
        }
        return fm;
    }

    protected void processActions(String actionKey, FlowModel flowModel) {

        try {
            log.debug("Received flow request with action key '" + actionKey + "'");
            String actionFlowString = OgcFlowDefinitions.findFlow(actionKey);
            String[] actionFlowArrray = actionFlowString.split(",");
            log.debug("Found " + actionFlowArrray.length + " action(s) for execution. But first, clearing root Json from Flow model...");
            flowModel.clearBeforeExecution();
            log.debug("FlowModel clear.");
            
            for (int i = 0; i < actionFlowArrray.length; i++) {
                String className = actionFlowArrray[i];
                Class<?> tmpClass = Class.forName(className);
                OGCActionHandler actionHandler =  (OGCActionHandler)tmpClass.newInstance();
                log.debug("Running action '" + actionHandler.getClass().getName() + "'");
                actionHandler.handleAction(flowModel);
                log.debug("Action '" + actionHandler.getClass().getName() +  "' ready.");
            }       
        } catch (OgcFlowException e) {
            /* Log error here */
            flowModel.putValueToRootJson("error", "true");
            flowModel.putValueToRootJson("error_msg", e.getError());
            log.error("Failed to execute action '" + actionKey + "'", e);
            //throw new RuntimeException("Failed to execute action '" + actionKey + "'", e);
        } catch (Exception e) {
            /* Log error here */
            flowModel.putValueToRootJson("error", "true");
            log.error("Failed to execute action '" + actionKey + "'", e);
        } catch (Error e) {
            /* In some cases we have had problems with NoClassDefFoundError which does not show in logs.
             * Problem is that if GeoTools is missing some jar NoClassDefFoundError is swallowed and
             * nothing shows up. This is why we try to handle that case here. */
            e.printStackTrace();
        }

    }
}
