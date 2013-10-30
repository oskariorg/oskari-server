package fi.nls.oskari.control.myplaces;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ProxyService;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * Routes myplaces tiles for current user.
 * Uses ProxyService with service key "myplacestile".
 * Handler returns binary response with content type "image/png".
 */
@OskariActionRoute("MyPlacesTile")
public class MyPlacesTileHandler extends ActionHandler {

    private final static Logger log = LogFactory.getLogger(MyPlacesTileHandler.class);

    final private static String SERVICE_KEY = "myplacestile";

    public void handleAction(ActionParameters params) throws ActionException {

        log.debug("Proxying to", SERVICE_KEY);
        // proxy config handles making external request with credentials
        final byte[] proxyResponse = ProxyService.proxyBinary(SERVICE_KEY, params);
        if (proxyResponse != null && proxyResponse.length > 0) {
            log.debug("Got:\n", proxyResponse.length, "bytes");
        } else {
            log.debug("Got empty response!");
        }

        try {
            final HttpServletResponse response = params.getResponse();
            // Cache for hour
            response.setHeader("Cache-Control", "must-revalidate, max-age=3600");
            response.setContentType("image/png");
            final OutputStream os = response.getOutputStream();
            os.write(proxyResponse);
        } catch (Exception e) {
            throw new ActionException("Failed to create image", e);
        }
    }

}
