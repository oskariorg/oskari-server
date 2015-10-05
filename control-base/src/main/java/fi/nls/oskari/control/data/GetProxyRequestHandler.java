package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ProxyService;
import fi.nls.oskari.util.ResponseHelper;


/**
 * Proxies connections to an url based on serviceId
 */
@OskariActionRoute("GetProxyRequest")
public class GetProxyRequestHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetProxyRequestHandler.class);
    private static final String PARAM_SERVICE = "serviceId";

    public void init() {
        ProxyService.init();
    }

    public void handleAction(ActionParameters params) throws ActionException {


        final String serviceKey = params.getHttpParam(PARAM_SERVICE, "");
        final String response = ProxyService.proxy(serviceKey, params);
        log.debug("Proxied to", serviceKey, "got:\n", response);
        ResponseHelper.writeResponse(params, response);
    }
}
