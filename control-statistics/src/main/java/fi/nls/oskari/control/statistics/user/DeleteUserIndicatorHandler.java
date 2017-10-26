package fi.nls.oskari.control.statistics.user;

import fi.mml.map.mapwindow.service.db.UserIndicatorService;
import fi.mml.map.mapwindow.service.db.UserIndicatorServiceImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Deletes indicator that the user has previously saved.
 * Only allows deletion of the users own indicators
 */
@OskariActionRoute("DeleteUserIndicator")
public class DeleteUserIndicatorHandler extends ActionHandler {
    private UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();
    private static final Logger log = LogFactory.getLogger(GetUserIndicatorsHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {
        // user indicators are user content so deleting one requires to be logged in
        params.requireLoggedInUser();

        int id  = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        UserIndicator ui = userIndicatorService.find(id);
        if(ui == null) {
            throw new ActionParamsException("Unknown indicator");
        }
        if(params.getUser().getId() != ui.getUserId()) {
            throw new ActionDeniedException("User has no right to delete indicator");
        }
        log.info("Deleting indicator", id, "belonging to user", ui.getUserId());
        userIndicatorService.delete(id);
        // write the removed indicator as response
        ResponseHelper.writeResponse(params, GetUserIndicatorsHandler.makeJson(ui));
    }
}
