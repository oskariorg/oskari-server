package fi.nls.oskari.control.data;


import fi.mml.portti.domain.permissions.WFSLayerPermissionsStore;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Response Oskari user uid in response header data
 */
@OskariActionRoute("GetCurrentUser")
public class GetCurrentUserHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetCurrentUserHandler.class);
    private static final String KEY_UID = "currentUserUid";


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        String result = "{}";

        //String jsessionid = params.getRequest().getSession().getId();

        params.getUser().getUuid();
        params.getResponse().setHeader("currentUserUid", params.getUser().getUuid());
        ResponseHelper.writeResponse(params, result);
        return;
    }
}
