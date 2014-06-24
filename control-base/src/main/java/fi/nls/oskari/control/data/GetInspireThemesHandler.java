package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;


/**
 * Lists all Inspire themes
 *
 * @deprecated Use fi.nls.oskari.control.data.InspireThemesHandler instead
 */
@OskariActionRoute("GetInspireThemes")
@Deprecated
public class GetInspireThemesHandler extends ActionHandler {

    private InspireThemesHandler handler = new InspireThemesHandler();

    @Override
    public void init() {
        super.init();
        handler.init();
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        handler.handleGet(params);
    }
}
