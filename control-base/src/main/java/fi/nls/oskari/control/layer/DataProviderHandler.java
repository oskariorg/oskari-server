package fi.nls.oskari.control.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import fi.nls.oskari.cache.CacheManager;

import org.json.JSONException;
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
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.RequestHelper;
import fi.nls.oskari.util.ResponseHelper;

import static fi.nls.oskari.control.ActionConstants.PARAM_NAME_PREFIX;

/**
 * Action route handler for single data provider related operations
 */
@OskariActionRoute("DataProvider")
public class DataProviderHandler extends RestActionHandler {

	private static final String PARAM_ID = "id";
	private static final String PARAM_DELETE_LAYERS = "deleteLayers";

	private DataProviderService dataProviderService = ServiceFactory.getDataProviderService();
	private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();

	@Override
	public void handleGet(ActionParameters params) throws ActionException {

		final int id = params.getRequiredParamInt(PARAM_ID);

		try {
			DataProvider d = dataProviderService.find(id);
			ResponseHelper.writeResponse(params, d.getAsJSON());
		} catch (Exception e) {
			throw new ActionException("Data provider query failed", e);
		}
	}

	@Override
	public void handleDelete(ActionParameters params) throws ActionException {

		final int id = params.getRequiredParamInt(PARAM_ID);
		final boolean deleteLayers = params.getRequiredParamBoolean(PARAM_DELETE_LAYERS);

		if (!dataProviderService.hasPermissionToUpdate(params.getUser(), id)) {
			throw new ActionDeniedException("Unauthorized user tried to remove data provider id=" + id);
		}
		
		List<OskariLayer> layers = mapLayerService.findByDataProviderId(id);
		
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
			DataProvider dataProvider = dataProviderService.find(id);
			if (dataProvider == null) {
				throw new ActionParamsException("Dataprovider not found with id " + id);
			}
			
			List<String> layerNamesToBeDeleted = deleteLayers ? layers.stream().map(OskariLayer::getName)
					.collect(Collectors.toList()) : new ArrayList<String>();
			
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

	private void flushLayerListCache() {
		CacheManager.getCache(GetMapLayerGroupsHandler.CACHE_NAME).flush(true);
	}

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
			try {
				JSONObject payload = params.getPayLoadJSON();
				dataProvider.setId(ConversionHelper.getInt(payload.optString("id"), -1));
				JSONObject locales;
				locales = payload.getJSONObject("locales");

				JSONObject defaultLang = locales.optJSONObject(PropertyUtil.getDefaultLanguage());
				if (defaultLang == null) {
					throw new ActionParamsException("No locale for default lang: " + PropertyUtil.getDefaultLanguage());
				}
				String name = defaultLang.optString("name");
				if (name == null || name.trim().isEmpty()) {
					throw new ActionParamsException("No name for default lang: " + PropertyUtil.getDefaultLanguage());
				}

				dataProvider.setLocale(locales);
			} catch (JSONException e) {
				throw new ActionException("Cannot poulate maplayer group from request", e);
			}
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
}
