package fi.nls.oskari.control.sotka;

import fi.mml.map.mapwindow.service.db.UserIndicatorService;
import fi.mml.map.mapwindow.service.db.UserIndicatorServiceImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.indicator.UserIndicator;

/**
 * Created with IntelliJ IDEA.
 * User: EVAARASMAKI
 * Date: 22.11.2013
 * Time: 9:25
 * To change this template use File | Settings | File Templates.
 */

//@OskariActionRoute("SaveUserIndicator")
public class SaveUserIndicatorHandler extends ActionHandler {


    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();

    protected static String PARAM_INDICATOR_ID = "id";
    private static String PARAM_INDICATOR_TITLE = "title";
    private static String PARAM_INDICATOR_SOURCE = "source";
    private static String PARAM_INDICATOR_MATERIAL = "material"; //WMS- layer
    private static String PARAM_INDICATOR_YEAR = "year";
    private static String PARAM_INDICATOR_DATA = "data";
    private static String PARAM_INDICATOR_PUBLISHED = "published";

    public void handleAction(ActionParameters params) throws ActionException {
        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }

        int id = Integer.parseInt(params.getHttpParam(PARAM_INDICATOR_ID, "-1"));

        UserIndicator ui = populateUi(params);

        if (id != -1) {
            //update
           ui.setId(id);
           userIndicatorService.update(ui);
        } else {
            //insert
            userIndicatorService.insert(ui);
        }
    }

    private UserIndicator populateUi(ActionParameters params) {
        UserIndicator ui = new UserIndicator();

        ui.setUserId(params.getUser().getId());
        ui.setTitle(params.getHttpParam(PARAM_INDICATOR_TITLE));
        ui.setSource(params.getHttpParam(PARAM_INDICATOR_SOURCE));
        ui.setMaterial(Long.parseLong(params.getHttpParam(PARAM_INDICATOR_MATERIAL)));
        ui.setYear(Integer.parseInt(params.getHttpParam(PARAM_INDICATOR_YEAR)));
        ui.setData(params.getHttpParam(PARAM_INDICATOR_DATA));
        ui.setPublished(Boolean.parseBoolean(params.getHttpParam(PARAM_INDICATOR_PUBLISHED)));

        return ui;
    }
}
