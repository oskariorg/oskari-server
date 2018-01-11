package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
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

    private final DataProviderService dataProviderService = ServiceFactory.getDataProviderService();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final HttpServletRequest request = params.getRequest();
        try {
            final int dataProviderId = params.getHttpParam(PARAM_ID, -1);
            final DataProvider dataProvider = new DataProvider();
            dataProvider.setId(dataProviderId);
            handleLocalizations(dataProvider, PARAM_NAME_PREFIX, request);
            if (dataProvider.getLocale() == null) {
                throw new ActionParamsException("Missing names for layer dataprovider group!");
            }

            // ************** UPDATE ************************
            if (dataProviderId != -1) {
                if (!dataProviderService.hasPermissionToUpdate(params.getUser(), dataProviderId)) {
                    throw new ActionDeniedException("Unauthorized user tried to update layer dataprovider group - id=" + dataProviderId);
                }
                dataProviderService.update(dataProvider);
                ResponseHelper.writeResponse(params, dataProvider.getAsJSON());
            }
            // ************** INSERT ************************
            else if (params.getUser().isAdmin()) {
                final int id = dataProviderService.insert(dataProvider);
                dataProvider.setId(id);
                ResponseHelper.writeResponse(params, dataProvider.getAsJSON());
            } else {
                throw new ActionDeniedException("Unauthorized user tried to update layer dataprovider group - id=" + dataProviderId);
            }

        } catch (Exception e) {
            throw new ActionException("Couldn't update/insert map layer dataprovider group", e);
        }
    }

    private void handleLocalizations(final DataProvider lc, final String nameprefix, final HttpServletRequest request) {
        final Map<String, String> parsed = RequestHelper.parsePrefixedParamsMap(request, nameprefix);
        for(Map.Entry<String, String> entry : parsed.entrySet()) {
            lc.setName(entry.getKey(), entry.getValue());
        }
    }
}
