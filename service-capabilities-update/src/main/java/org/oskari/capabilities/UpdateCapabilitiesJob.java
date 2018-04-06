package org.oskari.capabilities;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.worker.ScheduledJob;

/**
 * ScheludedJob that updates Capabilities of layers
 * <ul>
 * <li>Updates oskari_capabilities_cache rows</li>
 * <li>Updates OskariLayer objects via #setCapabilities()</li>
 * </ul>
 */
@Oskari("UpdateCapabilitiesJob")
public class UpdateCapabilitiesJob extends ScheduledJob {

    private static final Logger LOG = LogFactory.getLogger(UpdateCapabilitiesJob.class);

    private final OskariLayerService layerService;
    private final CapabilitiesUpdateService capabilitiesUpdateService;
    private final ViewService viewService;

    public UpdateCapabilitiesJob() {
        this(new OskariLayerServiceIbatisImpl(),
                new CapabilitiesCacheServiceMybatisImpl(),
                new ViewServiceIbatisImpl());
    }

    public UpdateCapabilitiesJob(OskariLayerService layerService,
            CapabilitiesCacheService capabilitiesCacheService,
            ViewService viewService) {
        this.layerService = layerService;
        this.capabilitiesUpdateService = new CapabilitiesUpdateService(
                layerService, capabilitiesCacheService);
        this.viewService = viewService;
    }

    @Override
    public void execute(Map<String, Object> params) {
        LOG.info("Starting UpdateCapabilitiesJob");

        final Set<String> systemCRSs;
        try {
            systemCRSs = ViewHelper.getSystemCRSs(viewService);
        } catch (ServiceException e) {
            LOG.warn("Failed to get systemCRSs", e);
            return;
        }

        List<OskariLayer> layersToUpdate = layerService.findAllWithPositiveUpdateRateSec().stream()
                .filter(layer -> shouldUpdate(layer))
                .collect(Collectors.toList());

        capabilitiesUpdateService.updateCapabilities(layersToUpdate, systemCRSs);
    }

    protected static boolean shouldUpdate(OskariLayer layer) {
        Date lastUpdated = layer.getCapabilitiesLastUpdated();
        if (lastUpdated == null) {
            LOG.debug("Should update layer:", layer.getId(), "last updated unknown");
            return true;
        }
        int rate = layer.getCapabilitiesUpdateRateSec();
        long nextUpdate = lastUpdated.getTime() + TimeUnit.SECONDS.toMillis(rate);
        long now = System.currentTimeMillis();
        LOG.debug("Layer:", layer.getId(), "next scheduled update is:", nextUpdate, "now is", now);
        if (nextUpdate <= now) {
            return true;
        }
        LOG.debug("Skipping layerId:", layer.getId(), "as recently updated");
        return false;
    }

}
