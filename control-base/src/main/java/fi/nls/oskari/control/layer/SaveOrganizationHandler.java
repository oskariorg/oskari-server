package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.LayerGroupService;
import fi.nls.oskari.util.RequestHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.ServiceFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;
import static fi.nls.oskari.control.ActionConstants.PARAM_NAME_PREFIX;

/**
 * Admin insert/update of class layer or class sub layer
 */
@OskariActionRoute("SaveOrganization")
public class SaveOrganizationHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(SaveOrganizationHandler.class);

    private final LayerGroupService layerGroupService = ServiceFactory.getLayerGroupService();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final HttpServletRequest request = params.getRequest();
        try {
            final int groupId = params.getHttpParam(PARAM_ID, -1);
            final DataProvider group = new DataProvider();
            group.setId(groupId);
            handleLocalizations(group, PARAM_NAME_PREFIX, request);
            if (group.getLocale() == null) {
                throw new ActionParamsException("Missing names for group!");
            }

            // ************** UPDATE ************************
            if (groupId != -1) {
                if (!layerGroupService.hasPermissionToUpdate(params.getUser(), groupId)) {
                    throw new ActionDeniedException("Unauthorized user tried to update layer group - id=" + groupId);
                }
                layerGroupService.update(group);
                ResponseHelper.writeResponse(params, group.getAsJSON());
            }
            // ************** INSERT ************************
            else if (params.getUser().isAdmin()) {
                final int id = layerGroupService.insert(group);
                group.setId(id);
                ResponseHelper.writeResponse(params, group.getAsJSON());
            } else {
                throw new ActionDeniedException("Unauthorized user tried to update layer group - id=" + groupId);
            }

        } catch (Exception e) {
            throw new ActionException("Couldn't update/insert map layer group", e);
        }
    }

    private void handleLocalizations(final DataProvider lc, final String nameprefix, final HttpServletRequest request) {
        final Map<String, String> parsed = RequestHelper.parsePrefixedParamsMap(request, nameprefix);
        for(Map.Entry<String, String> entry : parsed.entrySet()) {
            lc.setName(entry.getKey(), entry.getValue());
        }
    }
}
