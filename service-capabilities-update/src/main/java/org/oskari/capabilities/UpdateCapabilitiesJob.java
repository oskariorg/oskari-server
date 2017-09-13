package org.oskari.capabilities;

import java.sql.Timestamp;

import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import java.util.Map;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.worker.ScheduledJob;

/**
 * Updates layer capabilities
 */
@Oskari("UpdateCapabilities")
public class UpdateCapabilitiesJob extends ScheduledJob {

    private static final Logger LOG = LogFactory.getLogger(UpdateCapabilitiesJob.class);

    private final OskariLayerService layerService;
    private final CapabilitiesCacheService capabilitiesService;
    private final long maxAge;

    public UpdateCapabilitiesJob() {
        this(new OskariLayerServiceIbatisImpl(), new CapabilitiesCacheServiceMybatisImpl(), 0L);
    }

    public UpdateCapabilitiesJob(OskariLayerService layerService,
            CapabilitiesCacheService capabilitiesService, long maxAge) {
        this.layerService = layerService;
        this.capabilitiesService = capabilitiesService;
        this.maxAge = maxAge;
    }

    @Override
    public void execute(Map<String, Object> params) {
        for (OskariLayer layer : layerService.findAll()) {
            updateCapabilities(layer);
        }
    }

    protected void updateCapabilities(OskariLayer layer) {
        if (layer == null) {
            return;
        }
        try {
            switch (layer.getType()) {
            case OskariLayer.TYPE_WMS:
                updateCapabilitiesWMS(layer);
                break;
            case OskariLayer.TYPE_WMTS:
                updateCapabilitiesWMTS(layer);
                break;
            case OskariLayer.TYPE_WFS:
                updateCapabilitiesWFS(layer);
                break;
            default:
                LOG.info("Can't update capabilities of type:", layer.getType(),
                        "layer id:", layer.getId(), "skipping!");
                return;
            }
            layerService.update(layer);
        } catch (ServiceException e) {
            LOG.warn(e, "Failed to update capabilities of layer id:", layer.getId());
        }
    }

    private void updateCapabilitiesWMS(OskariLayer layer)
            throws ServiceException {
        OskariLayerCapabilities caps = getCapabilities(layer);
        OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(caps, layer);
    }

    private void updateCapabilitiesWMTS(OskariLayer layer)
            throws ServiceException {
        try {
            OskariLayerCapabilities caps = getCapabilities(layer);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(caps, layer, null);
        } catch (Exception e) {
            LOG.warn(e, "Failed to parse WMTS capabilities, layer: ", layer.getName());
        }
    }

    private void updateCapabilitiesWFS(OskariLayer layer)
            throws ServiceException {
        OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWFS(layer);
    }

    private OskariLayerCapabilities getCapabilities(final OskariLayer layer)
            throws ServiceException {
        if (maxAge <= 0L) {
            return capabilitiesService.getCapabilities(layer, true);
        }

        final Timestamp oldestAllowed = new Timestamp(System.currentTimeMillis() - maxAge);
        OskariLayerCapabilities capabilities = capabilitiesService.getCapabilities(layer, false);
        if (capabilities.getUpdated().before(oldestAllowed)) {
            return capabilities;
        }
        return capabilitiesService.getCapabilities(layer, true);
    }

}
