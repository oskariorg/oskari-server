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
 * HystrixCommand that loads BufferedImage from UserLayer via ProxyService
 */
public class CommandLoadImageAnalysis extends CommandLoadImageBase {

    private static final String FORMAT = "image/png";

    private final User user;
    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final double[] bbox;
    private final String srsName;

    public CommandLoadImageAnalysis(User user,
            PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srsName) {
        super("analysis_" + layer.getId());
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
        queryParams.put("wpsLayerId", Integer.toString(layer.getId()));

        HttpServletRequest request = new ModifiedHttpServletRequest(queryParams);
        ActionParameters params = new ActionParameters();
        params.setUser(user);
        params.setRequest(request);

        byte[] resp = ProxyService.proxyBinary("analysistile", params);
        InputStream input = new ByteArrayInputStream(resp);
        return ImageIO.read(input);
    }

    @Override
    public BufferedImage getFallback() {
        return null;
    }

}
