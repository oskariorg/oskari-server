package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.util.Customization;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@OskariActionRoute("Logo")
public class LogoHandler extends ActionHandler {

    /**
     * Streams the configured logo from file to frontend
     * @throws ActionException
     */
    public void handleAction(final ActionParameters params)
            throws ActionException {

        try  {
            String path = Customization.getLogoPath();
            // try detecting type from path like .png or .jpg
            String suffix = path.substring(path.lastIndexOf(".") + 1);
            ResponseHelper.writeResponse(params, HttpServletResponse.SC_OK, "image/" + suffix, Customization.getLogo());
        } catch (IOException e) {
            throw new ActionParamsException("Couldn't get logo", e.getMessage(), e);
        }
    }
}
