package fi.nls.oskari.control.data;


import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Response Oskari user uuid in response header data and user data as JSON in response body.
 */
@OskariActionRoute("GetCurrentUser")
public class GetCurrentUserHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetCurrentUserHandler.class);
    private static final String KEY_UID = "currentUserUid";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        params.getResponse().setHeader(KEY_UID, params.getUser().getUuid());
        ResponseHelper.writeResponse(params, params.getUser().toJSON());
    }
}
