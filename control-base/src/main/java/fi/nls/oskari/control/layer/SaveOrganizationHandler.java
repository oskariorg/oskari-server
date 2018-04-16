package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.RequestHelper;
import fi.nls.oskari.util.ResponseHelper;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

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
        final DataProvider dataProvider = new DataProvider();
        // hierarchical admin frontend sends separate params
        dataProvider.setId(params.getHttpParam(PARAM_ID, -1));
        dataProvider.setNames(RequestHelper.parsePrefixedParamsMap(request, PARAM_NAME_PREFIX));

        if (dataProvider.getLocale() == null) {
            // the classical layer admin sends JSON payload
            // locale as payload  { id: "124", locales: {en: "name en", fi: "name fi"}}
            JSONObject payload = params.getPayLoadJSON();
            dataProvider.setId(ConversionHelper.getInt(payload.optString("id"), -1));
            dataProvider.setNames(JSONHelper.getObjectAsMap(payload.optJSONObject("locales")));
        }

        if (dataProvider.getLocale() == null) {
            throw new ActionParamsException("Missing names for layer dataprovider!");
        }

        try {
            // ************** UPDATE ************************
            if (dataProvider.getId() != -1) {
                if (!dataProviderService.hasPermissionToUpdate(params.getUser(), dataProvider.getId())) {
                    throw new ActionDeniedException("Unauthorized user tried to update layer dataprovider - id=" + dataProvider.getId());
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
                throw new ActionDeniedException("Unauthorized user tried to update layer dataprovider - id=" + dataProvider.getId());
            }

        } catch (Exception e) {
            throw new ActionException("Couldn't update/insert map layer dataprovider", e);
        }
    }
}
