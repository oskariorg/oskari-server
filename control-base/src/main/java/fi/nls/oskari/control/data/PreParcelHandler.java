package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.myplaces.domain.ProxyRequest;
import fi.nls.oskari.map.myplaces.service.GeoServerProxyService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.RequestHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: EVAARASMAKI
 * Date: 21.11.2013
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */

@OskariActionRoute("PreParcel")
public class PreParcelHandler  extends ActionHandler {
    private static final Logger log = LogFactory
            .getLogger(PreParcelHandler.class);

    private GeoServerProxyService proxyService = new GeoServerProxyService();

    public void handleAction(ActionParameters params) throws ActionException {

    //    if (params.getUser().isGuest()) {
    //        throw new ActionDeniedException("Session expired");
    //    }

        final ProxyRequest req = new ProxyRequest();

        final HttpServletRequest request = params.getRequest();
        final Enumeration<String> parmNames = request.getParameterNames();
        while (parmNames.hasMoreElements()) {
            final String key = RequestHelper.cleanString(parmNames.nextElement());
            req.addParam(key, params.getHttpParam(key));
        }

        // preparcel needs geoserver auth
        req.setUrl(PropertyUtil.get("preparcel.oskari.url"));
        req.setUserName(PropertyUtil.get("preparcel.user"));
        req.setPassword(PropertyUtil.get("preparcel.password"));

        final String methodName = request.getMethod();
        req.setMethod(methodName);
        if (methodName.equals("POST")) {
            for (Enumeration<String> e = request.getHeaderNames(); e
                    .hasMoreElements();) {
                final String key = e.nextElement().toString();
                final String value = request.getHeader(key);
                req.addHeader(key,  Jsoup.clean(value, Whitelist.none()));
            }
            try {
                final String postData = IOHelper.readString(request.getInputStream());
                //TODO: kvp_uid check to post data
                req.setPostData(postData);
            } catch (IOException e) {
                throw new ActionException("Couldn't read POST data from request", e);
            }
        }
        try {
            final String response = proxyService.proxy(req);
            ResponseHelper.writeResponse(params, response);
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request to GeoServer", e);
        }
    }

}