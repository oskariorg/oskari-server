package fi.nls.oskari.control.layer;

import java.util.List;

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
		
		if(!dataProviderService.hasPermissionToUpdate(params.getUser(), id)) {
            throw new ActionDeniedException("Unauthorized user tried to remove data provider id=" + id);
        }
		
		if(deleteLayers) {
			deleteDataProviderAndLayers(id, params);
		} else {
			deleteDataProvider(id, params);
		}
	}
	
	/**
	 * Method deletes data provider.
	 * Data provider is removed from map layers belonging to the data provider prior delete.
	 *
	 * @param int id
	 * @param ActionParameters params
	 * @throws ActionException
	 */
	private void deleteDataProvider(int id, ActionParameters params) throws ActionException {
		try {
			DataProvider dataProvider = dataProviderService.find(id);
            if(dataProvider == null) {
                throw new ActionParamsException("Dataprovider not found with id " + id);
            }
			List<OskariLayer> layers = mapLayerService.findByDataProviderId(id);
			layers.stream().forEach(layer -> {
				layer.removeDataprovider(id);
				mapLayerService.update(layer);
			});
			dataProviderService.delete(id);
			AuditLog.user(params.getClientIp(), params.getUser())
	            .withParam("id", dataProvider.getId())
	            .withParam("name", dataProvider.getName(PropertyUtil.getDefaultLanguage()))
	            .deleted(AuditLog.ResourceType.DATAPROVIDER);

			// write deleted organization as response
			ResponseHelper.writeResponse(params, dataProvider.getAsJSON());
		} catch (Exception e) {
            throw new ActionException("Couldn't delete data provider with id:" + id, e);
        }
	}
	/**
	 * Method deletes data provider and map layers which belong to the data provider.
	 *
	 * @param int id
	 * @param ActionParameters params
	 * @throws ActionException
	 */
	private void deleteDataProviderAndLayers(int id, ActionParameters params) throws ActionException {
		try {
            DataProvider organization = dataProviderService.find(id);
            if(organization == null) {
                throw new ActionParamsException("Dataprovider not found with id " + id);
            }
            // cascade in db will handle that layers are deleted
            dataProviderService.delete(id);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", organization.getId())
                    .withParam("name", organization.getName(PropertyUtil.getDefaultLanguage()))
                    .deleted(AuditLog.ResourceType.DATAPROVIDER);

            // write deleted organization as response
            ResponseHelper.writeResponse(params, organization.getAsJSON());
        } catch (Exception e) {
            throw new ActionException("Couldn't delete data provider and its map layers - id:" + id, e);
        }
	}
}
