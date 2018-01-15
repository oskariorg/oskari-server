package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.statistics.user.StatisticalIndicatorService;

/**
 * Deletes indicator that the user has previously saved.
 * Only allows deletion of the users own indicators
 */
@OskariActionRoute("DeleteUserIndicator")
public class DeleteUserIndicatorHandler extends ActionHandler {
    private static final Logger LOG = LogFactory.getLogger(GetUserIndicatorsHandler.class);
    private StatisticalIndicatorService indicatorService;

    @Override
    public void init() {
        super.init();
        if (indicatorService == null) {
            indicatorService = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
        }
    }

    public void handleAction(ActionParameters params) throws ActionException {
        // user indicators are user content so deleting one requires to be logged in
        params.requireLoggedInUser();

        int id = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        if (!indicatorService.delete(id, params.getUser().getId())) {
            throw new ActionParamsException("Unknown indicator/not the owner of the indicator.");
        }
        LOG.info("Deleted indicator", id);
        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("deleted", id));
    }
}
