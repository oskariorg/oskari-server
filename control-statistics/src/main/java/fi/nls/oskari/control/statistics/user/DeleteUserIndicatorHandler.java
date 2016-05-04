package fi.nls.oskari.control.statistics.user;

import fi.mml.map.mapwindow.service.db.UserIndicatorService;
import fi.mml.map.mapwindow.service.db.UserIndicatorServiceImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: APELTONEN
 * Date: 31.12.2013
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */
@OskariActionRoute("DeleteUserIndicator")
public class DeleteUserIndicatorHandler extends ActionHandler {
    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();
    private static String PARAM_INDICATOR_ID = "id";
    private static final Logger log = LogFactory.getLogger(GetUserIndicatorsHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {
        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        int id  = -1;
        try {
            id = Integer.parseInt(params.getHttpParam(PARAM_INDICATOR_ID, "-1"));
        } catch (NumberFormatException nfe) {
            throw new ActionException("Invalid number");
        }

        if (id != -1) {
            UserIndicator ui = userIndicatorService.find(id);
            if (ui != null && (params.getUser().getId() == ui.getUserId())) {
                log.debug("Deleting indicator " + id + " belonging to user " + ui.getUserId());
                userIndicatorService.delete(id);
            } else {
                throw new ActionDeniedException("User has no right to delete indicator");
            }
        }
    }
}
