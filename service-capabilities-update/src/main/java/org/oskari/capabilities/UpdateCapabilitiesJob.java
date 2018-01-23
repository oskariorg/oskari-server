package org.oskari.capabilities;

import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import fi.mml.map.mapwindow.service.wms.WebMapService;
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
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.worker.ScheduledJob;

/**
 * ScheludedJob that updates Capabilities of WMS and WMTS layers
 * <ul>
 * <li>Updates oskari_capabilities_cache rows</li>
 * <li>Updates OskariLayer objects via #setCapabilities()</li>
 * </ul>
 */
@Oskari("UpdateCapabilitiesJob")
public class UpdateCapabilitiesJob extends ScheduledJob {

    private static final Logger LOG = LogFactory.getLogger(UpdateCapabilitiesJob.class);

    private final OskariLayerService layerService;
    private final CapabilitiesCacheService capabilitiesCacheService;
    private final ViewService viewService;

    public UpdateCapabilitiesJob() {
        this(new OskariLayerServiceIbatisImpl(),
                new CapabilitiesCacheServiceMybatisImpl(),
                new ViewServiceIbatisImpl());
    }

    public UpdateCapabilitiesJob(OskariLayerService layerService,
            CapabilitiesCacheService capabilitiesService,
            ViewService viewService) {
        this.layerService = layerService;
        this.capabilitiesCacheService = capabilitiesService;
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

        layerService.findAllWithPositiveUpdateRateSec().stream()
                .filter(layer -> canUpdate(layer.getType()))
                .filter(layer -> shouldUpdate(layer))
                .collect(groupingBy(layer -> new UrlTypeVersion(layer)))
                .forEach((utv, layers) -> updateCapabilities(utv, layers, systemCRSs));
    }

    protected static boolean canUpdate(String type) {
        switch (type) {
        case OskariLayer.TYPE_WMS:
        case OskariLayer.TYPE_WMTS:
            return true;
        default:
            return false;
        }
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

    private void updateCapabilities(UrlTypeVersion utv,
            List<OskariLayer> layers, Set<String> systemCRSs) {
        final String url = utv.url;
        final String type = utv.type;
        final String version = utv.version;
        final String user = layers.get(0).getUsername();
        final String pass = layers.get(0).getPassword();

        int[] ids = layers.stream().mapToInt(OskariLayer::getId).toArray();
        LOG.debug("Updating Capabilities for a group of layers - url:", url,
                "type:", type, "version:", version, "ids:", Arrays.toString(ids));

        final String data;
        try {
            data = capabilitiesCacheService.getCapabilities(url, type, user, pass, version, true).getData();
        } catch (ServiceException e) {
            LOG.warn(e, "Could not find get Capabilities, url:", url,
                    "type:", type, "version:", version, "ids:", Arrays.toString(ids));
            return;
        }

        switch (type) {
        case OskariLayer.TYPE_WMS:
            updateWMSLayers(layers, data, systemCRSs);
            break;
        case OskariLayer.TYPE_WMTS:
            updateWMTSLayers(layers, data, systemCRSs);
            break;
        }
    }

    private void updateWMSLayers(List<OskariLayer> layers, String data, Set<String> systemCRSs) {
        for (OskariLayer layer : layers) {
            WebMapService wms = OskariLayerCapabilitiesHelper.parseWMSCapabilities(data, layer);
            if (wms == null) {
                LOG.warn("Failed to parse Capabilities for layerId:", layer.getId());
                continue;
            }
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wms, layer, systemCRSs);
            layerService.update(layer);
        }
    }

    private void updateWMTSLayers(List<OskariLayer> layers, String data, Set<String> systemCRSs) {
        final WMTSCapabilities wmts;
        try {
            wmts = WMTSCapabilitiesParser.parseCapabilities(data);
        } catch (XMLStreamException | IllegalArgumentException e) {
            int[] ids = layers.stream().mapToInt(OskariLayer::getId).toArray();
            LOG.warn(e, "Failed to parse WMTS GetCapabilities ids:", Arrays.toString(ids));
            return;
        }

        for (OskariLayer layer : layers) {
            try {
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(wmts, layer, null, systemCRSs);
                layerService.update(layer);
            } catch (IllegalArgumentException e) {
                LOG.warn(e, "Failed to update layerId:", layer.getId());
            }
        }
    }

    private static class UrlTypeVersion {

        private final String url;
        private final String type;
        private final String version;

        private UrlTypeVersion(OskariLayer layer) {
            url = layer.getSimplifiedUrl(true);
            type = layer.getType();
            version = layer.getVersion();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof UrlTypeVersion)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            UrlTypeVersion s = (UrlTypeVersion) o;
            return url.equals(s.url)
                    && type.equals(s.type)
                    && version.equals(s.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, type, version);
        }

    }

}
