package fi.nls.oskari.control.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.oskari.cache.CacheManager;
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
}
