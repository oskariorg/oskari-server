package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.mml.map.mapwindow.service.db.InspireThemeServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**
 * Lists all Inspire themes
 * @deprecated Use fi.nls.oskari.control.data.InspireThemesHandler instead
 */
@OskariActionRoute("GetInspireThemes")
@Deprecated
public class GetInspireThemesHandler extends ActionHandler {

    //private InspireThemeService inspireThemeService = new InspireThemeServiceIbatisImpl();
    private InspireThemesHandler handler = new InspireThemesHandler();


    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        handler.handleGet(params);
    }
}
