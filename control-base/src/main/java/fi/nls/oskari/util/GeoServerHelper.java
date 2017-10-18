package fi.nls.oskari.util;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.map.myplaces.domain.ProxyRequest;
import fi.nls.oskari.map.myplaces.service.GeoServerProxyService;
import org.apache.axiom.om.OMElement;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class GeoServerHelper {

    private static GeoServerProxyService proxyService = new GeoServerProxyService();

    public static void sendRequest(final OMElement xml) throws Exception {
        final ProxyRequest req = new ProxyRequest();
        req.setUrl(PropertyUtil.get("myplaces.ows.url"));
        req.setUserName(PropertyUtil.get("myplaces.user"));
        req.setPassword(PropertyUtil.get("myplaces.password"));
        req.setMethod(req.getMethod());
        req.setPostData(XmlHelper.toString(xml));

        try {
            final String response = proxyService.proxy(req);
        } catch (Exception e) {
            throw new Exception("Couldn't proxy request to GeoServer", e);
        }
    }
}
