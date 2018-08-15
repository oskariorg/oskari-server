package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.GetMapBuilder;
import org.oskari.print.util.ModifiedHttpServletRequest;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ProxyService;

/**
 * HystrixCommand that loads BufferedImage from GeoServer via ProxyService
 */
public abstract class CommandLoadImageProxyService extends CommandLoadImageBase {

    protected static final String FORMAT = "image/png";

    protected final User user;
    protected final PrintLayer layer;
    protected final int width;
    protected final int height;
    protected final double[] bbox;
    protected final String srsName;

    public CommandLoadImageProxyService(User user,
            PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srsName) {
        super("geoserver");
        this.user = user;
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.bbox = bbox;
        this.srsName = srsName;
    }

    @Override
    public BufferedImage run() throws Exception {
        Map<String, String> queryParams = new GetMapBuilder()
                .version(layer.getVersion())
                .layer(layer.getName())
                .bbox(bbox)
                .crs(srsName)
                .width(width)
                .height(height)
                .format(FORMAT)
                .transparent(true)
                .toParamMap();
        queryParams.put(getIdParamName(), Integer.toString(layer.getId()));

        HttpServletRequest request = new ModifiedHttpServletRequest(queryParams);
        ActionParameters params = new ActionParameters();
        params.setUser(user);
        params.setRequest(request);

        byte[] resp = ProxyService.proxyBinary(getProxyServiceKey(), params);
        InputStream input = new ByteArrayInputStream(resp);
        return ImageIO.read(input);
    }

    @Override
    public BufferedImage getFallback() {
        return null;
    }

    public abstract String getIdParamName();
    public abstract String getProxyServiceKey();

}
