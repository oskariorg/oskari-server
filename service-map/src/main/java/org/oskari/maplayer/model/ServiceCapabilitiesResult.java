package org.oskari.maplayer.model;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to write JSON for admin functionality when adding layers
 */
public class ServiceCapabilitiesResult {

    private static final Logger log = LogFactory.getLogger(ServiceCapabilitiesResult.class);

    private String title;
    private List<MapLayerAdminOutput> layers;
    private List<String> layersWithErrors;
    private Map<String, List<Integer>> existingLayers;
    private List<MapLayerStructure> structure;
    private String version;
    private String currentSrs = PropertyUtil.get("oskari.native.srs", "EPSG:4326");

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, List<Integer>> getExistingLayers() {
        return existingLayers;
    }

    public void setExistingLayers(Map<String, List<Integer>> existingLayers) {
        this.existingLayers = existingLayers;
    }

    public List<String> getLayersWithErrors() {
        return layersWithErrors;
    }

    public void setLayersWithErrors(List<String> layersWithErrors) {
        this.layersWithErrors = layersWithErrors;
    }

    /**
     * Returns layers not in list but JSON object with name as key and the layer JSON as value
     * @return
     */
    public Map<String, MapLayerAdminOutput> getLayers() {
        BinaryOperator<MapLayerAdminOutput> merge = (a, b) -> {
            String url = a.getUrl();
            String name = a.getName();
            log.warn("Duplicate layer name, service url:", url, "layer name:", name);
            // Keep the one we already have
            return a;
        };
        return layers.stream().collect(
                Collectors.toMap(MapLayerAdminOutput::getName, Function.identity(), merge));
    }

    public void setLayers(List<MapLayerAdminOutput> layers) {
        this.layers = layers;
    }

    public void setCurrentSrs(String currentSrs) {
        this.currentSrs = currentSrs;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getCapabilitiesFailed() {
        if (layers == null) {
            return Collections.emptyList();
        }
        return layers.stream()
                .filter(l -> l.getCapabilities() == null || l.getCapabilities().isEmpty())
                .map(l -> l.getName())
                .collect(Collectors.toList());
    }

    // structure is only used for wms-layers
    public List<MapLayerStructure> getStructure() {
        return structure;
    }
    public void setStructure(List<MapLayerStructure> structure) {
        this.structure = structure;
    }

    public List<String> getUnsupportedLayers() {
        if (layers == null) {
            return Collections.emptyList();
        }
        return layers.stream()
                .filter(l -> !getCapabilitiesFailed().contains(l.getName()))
                .filter(l -> l.getCapabilities() != null)
                .filter(l -> !supportsSRS(l, currentSrs))
                .map(l -> l.getName())
                .collect(Collectors.toList());
    }

    private boolean supportsSRS(MapLayerAdminOutput layer, String srs) {
        List<String> supportedSRS = (List<String>) layer.getCapabilities()
                .getOrDefault("srs", Collections.emptyList());
        return supportedSRS.contains(srs);
    }
}
