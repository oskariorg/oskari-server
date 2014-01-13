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
 */
@OskariActionRoute("GetInspireThemes")
public class GetInspireThemesHandler extends ActionHandler {

    private InspireThemeService inspireThemeService = new InspireThemeServiceIbatisImpl();


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        try {
            final List<InspireTheme> inspireThemes = inspireThemeService.findAll();
            final JSONArray list = new JSONArray();
            for (InspireTheme theme : inspireThemes) {
                list.put(theme.getAsJSON());
            }
            final JSONObject result = new JSONObject();
            JSONHelper.putValue(result, "inspire", list);
            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            throw new ActionException("Inspire themes listing failed", e);
        }
    }
}
