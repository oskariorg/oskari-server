package fi.nls.oskari.control.data;

import java.util.List;

import fi.nls.oskari.annotation.OskariActionRoute;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.mml.map.mapwindow.service.db.InspireThemeServiceIbatisImpl;

import fi.nls.oskari.domain.map.InspireTheme;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;

import fi.nls.oskari.util.ResponseHelper;

//import fi.nls.oskari.log.Logger;

/**
 * GET Inspire themes
 * 
 */
@OskariActionRoute("GetInspireThemes")
public class GetInspireThemesHandler extends ActionHandler {

    private InspireThemeService inspireThemeService = new InspireThemeServiceIbatisImpl();

    // private static final Logger log = LogFactory.getLogger(GetInspireThemes.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        try {
            final JSONObject themeJSON = new JSONObject();
            List<InspireTheme> inspireThemes = inspireThemeService.findAll();

            for (InspireTheme it : inspireThemes) {
                JSONObject themeProperties = new JSONObject();

                themeProperties.put("id", it.getId());
                themeProperties.put("nameFi", it.getNameFi());
                themeProperties.put("nameSv", it.getNameSv());
                themeProperties.put("nameEn", it.getNameEn());
                themeJSON.accumulate(String.valueOf(it.getId()),
                        themeProperties);
            }

            ResponseHelper.writeResponse(params, themeJSON);

        } catch (Exception e) {
            throw new ActionException("Inspire themes listing failed", e);
        }


    }

}
