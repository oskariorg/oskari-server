package org.oskari.print.loader;

import org.oskari.print.request.PrintLayer;

import fi.nls.oskari.domain.User;

/**
 * HystrixCommand that loads BufferedImage from MyPlaces via ProxyService
 */
public class CommandLoadImageMyPlaces extends CommandLoadImageProxyService {

    public CommandLoadImageMyPlaces(User user,
            PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srsName) {
        super(user, layer, width, height, bbox, srsName);
    }

    @Override
    public String getIdParamName() {
        return "myCat";
    }

    @Override
    public String getProxyServiceKey() {
        return "myplacestile";
    }

}
