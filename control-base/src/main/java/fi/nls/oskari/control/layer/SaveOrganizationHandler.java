package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.util.*;

import javax.servlet.http.HttpServletRequest;

import org.oskari.log.AuditLog;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;
import static fi.nls.oskari.control.ActionConstants.PARAM_NAME_PREFIX;

/**
 * Admin insert/update of class layer or class sub layer
 */
@OskariActionRoute("SaveOrganization")
public class SaveOrganizationHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(SaveOrganizationHandler.class);

    private final DataProviderService dataProviderService = ServiceFactory.getDataProviderService();

    public void handlePut(ActionParameters params) throws ActionException {
        handlePost(params);
    }
    @Override
    public void handlePost(ActionParameters params) throws ActionException {

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
                AuditLog.user(params.getClientIp(), params.getUser())
                        .withParam("id", dataProvider.getId())
                        .withParam("name", dataProvider.getName(PropertyUtil.getDefaultLanguage()))
                        .updated(AuditLog.ResourceType.DATAPROVIDER);

                ResponseHelper.writeResponse(params, dataProvider.getAsJSON());
            }
            // ************** INSERT ************************
            else if (params.getUser().isAdmin()) {
                final int id = dataProviderService.insert(dataProvider);
                dataProvider.setId(id);
                AuditLog.user(params.getClientIp(), params.getUser())
                        .withParam("id", dataProvider.getId())
                        .withParam("name", dataProvider.getName(PropertyUtil.getDefaultLanguage()))
                        .added(AuditLog.ResourceType.DATAPROVIDER);

                ResponseHelper.writeResponse(params, dataProvider.getAsJSON());
            } else {
                throw new ActionDeniedException("Unauthorized user tried to update layer dataprovider - id=" + dataProvider.getId());
            }
            flushLayerListCache();
        } catch (Exception e) {
            throw new ActionException("Couldn't update/insert map layer dataprovider", e);
        }
    }

    private void flushLayerListCache() {
        CacheManager.getCache(GetMapLayerGroupsHandler.CACHE_NAME).flush(true);
    }
}
