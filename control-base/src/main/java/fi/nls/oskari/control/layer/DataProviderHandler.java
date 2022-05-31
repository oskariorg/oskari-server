package fi.nls.oskari.control.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import fi.nls.oskari.cache.CacheManager;

import org.json.JSONObject;
import org.oskari.log.AuditLog;
import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * CRUD for Maplayer dataproviders. Get is callable by anyone, other methods require
 * admin user.
 */
@OskariActionRoute("DataProvider")
public class DataProviderHandler extends RestActionHandler {

    private static final String PARAM_ID = "id";
    private static final String PARAM_DELETE_LAYERS = "deleteLayers";
    private static final String KEY_LOCALES = "locales";

    private DataProviderService dataProviderService = ServiceFactory.getDataProviderService();
    private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final int id = params.getRequiredParamInt(PARAM_ID);
        ResponseHelper.writeResponse(params, getById(id).getAsJSON());
    }

    private DataProvider getById(int id) throws ActionParamsException {
        DataProvider d = dataProviderService.find(id);
        if (d == null) {
            throw new ActionParamsException("No provider for id=" + id);
        }
        return d;
    }

    /*
    Receives JSON as payload
    {
        "id":"29",
        "locales": {
            "fi":{
                "name":"name in lang"
                "desc":"desc in lang"
            },
            "sv":{
                "name":"name in lang"
                "desc":"desc in lang"
            },
            "en":{
                "name":"name in lang"
                "desc":"desc in lang"
            }
        }
    }
     */
    public void handlePut(ActionParameters params) throws ActionException {
        int id = params.getRequiredParamInt(PARAM_ID);
        if (!dataProviderService.hasPermissionToUpdate(params.getUser(), id)) {
            throw new ActionDeniedException("Unauthorized user tried to update layer dataprovider - id=" + id);
        }
        DataProvider provider = getById(id);
        JSONObject json = params.getPayLoadJSON();
        JSONObject locales = json.optJSONObject(KEY_LOCALES);
        validateLocales(locales);
        provider.setLocale(locales);

        dataProviderService.update(provider);
        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", provider.getId())
                .withParam("name", provider.getName(PropertyUtil.getDefaultLanguage()))
                .updated(AuditLog.ResourceType.DATAPROVIDER);

        flushLayerListCache();
        ResponseHelper.writeResponse(params, provider.getAsJSON());
    }

    private void validateLocales(JSONObject locales) throws ActionParamsException {
        if (locales == null) {
            throw new ActionParamsException("No locales for provider");
        }
        JSONObject defaultLang = locales.optJSONObject(PropertyUtil.getDefaultLanguage());
        if (defaultLang == null) {
            throw new ActionParamsException("No locale for default lang: " + PropertyUtil.getDefaultLanguage());
        }
        String name = defaultLang.optString(KEY_NAME);
        if (name == null || name.trim().isEmpty()) {
            throw new ActionParamsException("No name for default lang: " + PropertyUtil.getDefaultLanguage());
        }
    }
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireAdminUser();

        JSONObject json = params.getPayLoadJSON();
        final DataProvider provider = new DataProvider();
        JSONObject locales = json.optJSONObject(KEY_LOCALES);
        validateLocales(locales);
        provider.setLocale(locales);

        final int id = dataProviderService.insert(provider);
        provider.setId(id);
        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", provider.getId())
                .withParam("name", provider.getName(PropertyUtil.getDefaultLanguage()))
                .added(AuditLog.ResourceType.DATAPROVIDER);

        flushLayerListCache();
        ResponseHelper.writeResponse(params, provider.getAsJSON());
    }

    private void flushLayerListCache() {
        CacheManager.getCache(GetMapLayerGroupsHandler.CACHE_NAME).flush(true);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final int id = params.getRequiredParamInt(PARAM_ID);
        if (!dataProviderService.hasPermissionToUpdate(params.getUser(), id)) {
            throw new ActionDeniedException("Unauthorized user tried to remove data provider id=" + id);
        }

        List<OskariLayer> layers = mapLayerService.findByDataProviderId(id);
        final boolean deleteLayers = params.getRequiredParamBoolean(PARAM_DELETE_LAYERS);
        if (!deleteLayers) {
            removeProviderFromLayers(layers, id);
        }
        deleteDataProvider(layers, deleteLayers, id, params);
    }

    private void removeProviderFromLayers(List<OskariLayer> layers, int id) {
        layers.forEach(layer -> {
            layer.removeDataprovider(id);
            mapLayerService.update(layer);
        });
    }

    /**
     * Method deletes data provider.
     * Cascade in db will handle that layers for the data provider are also deleted
     *
     * @throws ActionException
     */
    private void deleteDataProvider(List<OskariLayer> layers, boolean deleteLayers, int id, ActionParameters params) throws ActionException {
        try {
            DataProvider dataProvider = getById(id);

            List<String> layerNamesToBeDeleted = deleteLayers ? layers.stream().map(OskariLayer::getName)
                    .collect(Collectors.toList()) : new ArrayList<>();

            dataProviderService.delete(id);

            AuditLog.user(params.getClientIp(), params.getUser()).withParam("id", dataProvider.getId())
                    .withParam("name", dataProvider.getName(PropertyUtil.getDefaultLanguage()))
                    .withMsg("map layers " + layerNamesToBeDeleted + " deleted with data provider" )
                    .deleted(AuditLog.ResourceType.DATAPROVIDER);
            flushLayerListCache();
            // write deleted organization as response
            ResponseHelper.writeResponse(params, dataProvider.getAsJSON());
        } catch (Exception e) {
            throw new ActionException("Couldn't delete data provider with id:" + id, e);
        }
    }


}
