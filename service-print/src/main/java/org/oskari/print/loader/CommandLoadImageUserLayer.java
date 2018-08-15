package org.oskari.print.loader;

import org.oskari.print.request.PrintLayer;

import fi.nls.oskari.domain.User;

/**
 * HystrixCommand that loads BufferedImage from UserLayer via ProxyService
 */
public class CommandLoadImageUserLayer extends CommandLoadImageProxyService {

    public CommandLoadImageUserLayer(User user,
            PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srsName) {
        super(user, layer, width, height, bbox, srsName);
    }

    @Override
    public String getIdParamName() {
        return "id";
    }

    @Override
    public String getProxyServiceKey() {
        return "userlayertile";
    }

}
