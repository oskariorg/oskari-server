package fi.nls.oskari.control.wfs;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;

/**
 * Returns prerendered image tiles for a WFS layer
 */
@OskariActionRoute("GET_PNG_MAP")
public class GetImageHandler extends FlowModelHandler {


    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        try {

            final String actionKey = params.getRequest().getParameter(KEY_ACTION);
            final FlowModel flowModel = createFlowModelFlowModel(params);
            // FIXME: tear down this internal processActions mapping
            processActions(actionKey, flowModel);

            final BufferedImage image = (BufferedImage)flowModel.get(KEY_IMAGE);
            final HttpServletResponse response = params.getResponse();
            // Cache for hour
            response.setHeader("Cache-Control", "must-revalidate, max-age=3600");
            response.setContentType("image/png");
            final OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (Exception e) {
            throw new ActionException("Failed to create image", e);
        }
    }
}
