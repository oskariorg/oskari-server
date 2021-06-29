package org.oskari.control.layer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionCommonException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.control.layer.model.LayerListResponse;
import org.oskari.control.layer.model.LayerOutput;
import org.oskari.control.layer.util.ModelHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An action route that returns layers for listing
 */
@OskariActionRoute("LayerList")
public class LayerListHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(LayerListHandler.class);

    private DataProviderService dataProviderService;

    public void setDataProviderService(DataProviderService service) {
        this.dataProviderService = service;
    }

    @Override
    public void init() {
        // setup services if they haven't been initialized
        /*
        if (layerService == null) {
            setLayerService(OskariComponentManager.getComponentOfType(OskariLayerService.class));
        }
        if (groupService == null) {
            setGroupService(ServiceFactory.getOskariMapLayerGroupService());
        }
        if (linkService == null) {
            setLinkService(new OskariLayerGroupLinkServiceMybatisImpl());
        }
         */
        if (dataProviderService == null) {
            setDataProviderService(OskariComponentManager.getComponentOfType(DataProviderService.class));
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        String language = params.getLocale().getLanguage();
        List<OskariLayer> layers = OskariLayerWorker.getLayersForUser(params.getUser(), false);
        // LayerOutput[] models =
        List<LayerOutput> models = layers.stream()
                .map(layer -> ModelHelper.getLayerForListing(layer, language))
                // .toArray(LayerOutput[]::new);
                .collect(Collectors.toList());
        LayerListResponse response = new LayerListResponse(models);
        response.setupProviders(dataProviderService.findAll(), language);
        // TODO: groups
        ResponseHelper.writeResponse(params, getString(response));
    }

    public static String getString(LayerListResponse output) throws ActionException {
        try {
            return ModelHelper.getMapper().writeValueAsString(output);
        } catch (Exception e) {
            throw new ActionCommonException("Error writing response", e);
        }
    }
}
