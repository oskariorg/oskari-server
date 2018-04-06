package org.oskari.capabilities;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import fi.mml.map.mapwindow.service.wms.LayerNotFoundInCapabilitiesException;
import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceParseException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;

public class CapabilitiesUpdateService {

    private static final Logger LOG = LogFactory.getLogger(UpdateCapabilitiesJob.class);

    private static final String ERR_LAYER_TYPE_UNSUPPORTED = "Layer type not supported for update";
    private static final String ERR_FAILED_TO_FETCH_CAPABILITIES = "Failed to get Capabilities data";
    private static final String ERR_LAYER_NOT_FOUND_IN_CAPABILITIES = "Could not find layer from Capabilities";
    private static final String ERR_FAILED_TO_PARSE_CAPABILITIES = "Failed to parse Capabilities";

    private final OskariLayerService layerService;
    private final CapabilitiesCacheService capabilitiesCacheService;

    public CapabilitiesUpdateService(OskariLayerService layerService,
            CapabilitiesCacheService capabilitiesService) {
        this.layerService = layerService;
        this.capabilitiesCacheService = capabilitiesService;
    }

    public List<CapabilitiesUpdateResult> updateCapabilities(List<OskariLayer> layersToUpdate, Set<String> systemCRSs) {
        List<CapabilitiesUpdateResult> results = new ArrayList<>(layersToUpdate.size());

        List<OskariLayer> updateableLayers = new ArrayList<>();
        for (OskariLayer layer : layersToUpdate) {
            if (canUpdate(layer.getType())) {
                updateableLayers.add(layer);
            } else {
                results.add(CapabilitiesUpdateResult.err(layer, ERR_LAYER_TYPE_UNSUPPORTED));
            }
        }

        Map<UrlTypeVersion, List<OskariLayer>> layersByUTV = updateableLayers.stream()
                .collect(groupingBy(layer -> new UrlTypeVersion(layer)));

        for (UrlTypeVersion utv : layersByUTV.keySet()) {
            List<OskariLayer> layers = layersByUTV.get(utv);
            updateCapabilities(utv, layers, systemCRSs, results);
        }

        return results;
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

    private void updateCapabilities(UrlTypeVersion utv,
            List<OskariLayer> layers, Set<String> systemCRSs, List<CapabilitiesUpdateResult> results) {
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
            for (OskariLayer layer : layers) {
                results.add(CapabilitiesUpdateResult.err(layer, ERR_FAILED_TO_FETCH_CAPABILITIES));
            }
            return;
        }

        switch (type) {
        case OskariLayer.TYPE_WMS:
            updateWMSLayers(layers, data, systemCRSs, results);
            break;
        case OskariLayer.TYPE_WMTS:
            updateWMTSLayers(layers, data, systemCRSs, results);
            break;
        }
    }

    private void updateWMSLayers(List<OskariLayer> layers, String data,
            Set<String> systemCRSs, List<CapabilitiesUpdateResult> results) {
        for (OskariLayer layer : layers) {
            try {
                WebMapService wms = OskariLayerCapabilitiesHelper.parseWMSCapabilities(data, layer);
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wms, layer, systemCRSs);
                layerService.update(layer);
                results.add(CapabilitiesUpdateResult.ok(layer));
            } catch (WebMapServiceParseException e) {
                LOG.warn(e, "Failed to update Capabilities for layerId:", layer.getId());
                results.add(CapabilitiesUpdateResult.err(layer, ERR_FAILED_TO_PARSE_CAPABILITIES));
            } catch (LayerNotFoundInCapabilitiesException e) {
                LOG.warn(e, "Failed to update Capabilities for layerId:", layer.getId());
                results.add(CapabilitiesUpdateResult.err(layer, ERR_LAYER_NOT_FOUND_IN_CAPABILITIES));
            }
        }
    }

    private void updateWMTSLayers(List<OskariLayer> layers, String data,
            Set<String> systemCRSs, List<CapabilitiesUpdateResult> results) {
        final WMTSCapabilities wmts;
        try {
            wmts = WMTSCapabilitiesParser.parseCapabilities(data);
        } catch (XMLStreamException | IllegalArgumentException e) {
            int[] ids = layers.stream().mapToInt(OskariLayer::getId).toArray();
            LOG.warn(e, "Failed to parse WMTS GetCapabilities for layerIds:", Arrays.toString(ids));
            for (OskariLayer layer : layers) {
                results.add(CapabilitiesUpdateResult.err(layer, ERR_FAILED_TO_PARSE_CAPABILITIES));
            }
            return;
        }

        for (OskariLayer layer : layers) {
            try {
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(wmts, layer, null, systemCRSs);
                layerService.update(layer);
                results.add(CapabilitiesUpdateResult.ok(layer));
            } catch (IllegalArgumentException e) {
                results.add(CapabilitiesUpdateResult.err(layer, e.getMessage()));
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
