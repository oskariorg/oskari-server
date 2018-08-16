package org.oskari.print.loader;

import org.oskari.print.request.PrintLayer;

import fi.nls.oskari.domain.User;

/**
 * HystrixCommand that loads BufferedImage from Analysis Layer via ProxyService
 */
public class CommandLoadImageAnalysis extends CommandLoadImageProxyService {

    public CommandLoadImageAnalysis(User user,
            PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srsName) {
        super(user, layer, width, height, bbox, srsName);
    }
    
    @Override
    public String getIdParamName() {
        return "wpsLayerId";
    }

    @Override
    public String getProxyServiceKey() {
        return "analysistile";
    }

}
