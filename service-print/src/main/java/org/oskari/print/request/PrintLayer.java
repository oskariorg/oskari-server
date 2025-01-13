package org.oskari.print.request;

import java.util.Optional;

import fi.nls.oskari.map.style.VectorStyleService;
import fi.nls.oskari.service.OskariComponentManager;
import org.json.JSONObject;
import org.oskari.service.user.UserLayerService;
import fi.nls.oskari.domain.map.OskariLayer;

public class PrintLayer {

    private static VectorStyleService getVectorStyleService() {
        return OskariComponentManager.getComponentOfType(VectorStyleService.class);
    }

    private final int zIndex;
    private String layerId;
    private OskariLayer oskariLayer;
    private String style;
    private int opacity;
    private Optional<UserLayerService> processor;
    private PrintTile[] tiles;
    private JSONObject customStyle;

    public PrintLayer(int zIndex) {
        this.zIndex = zIndex;
    }

    public int getZIndex() {
        return zIndex;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public OskariLayer getOskariLayer() {
        return oskariLayer;
    }

    public void setOskariLayer(OskariLayer oskariLayer) {
        this.oskariLayer = oskariLayer;
    }

    public int getId() {
        return oskariLayer.getId();
    }

    public String getType() {
        return oskariLayer.getType();
    }

    public String getName() {
        return oskariLayer.getName();
    }

    public String getUrl() {
        return oskariLayer.getUrl();
    }

    public String getStyle() {
        return style != null ? style : oskariLayer.getStyle();
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getVersion() {
        return oskariLayer.getVersion();
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public String getUsername() {
        return oskariLayer.getUsername();
    }

    public String getPassword() {
        return oskariLayer.getPassword();
    }

    public PrintTile[] getTiles() {
        return tiles;
    }

    public void setTiles(PrintTile[] tiles) {
        this.tiles = tiles;
    }

    public Optional<UserLayerService> getProcessor() {
        return processor == null ? Optional.empty() : processor;
    }

    public void setProcessor(Optional<UserLayerService> processor) {
        this.processor = processor;
    }

    public void setCustomStyle (JSONObject customStyle) { this.customStyle = customStyle; }

    public JSONObject getCustomStyle () { return customStyle; }

    // TODO: print should support optionalStyles and whole style should be returned
    // For now this handles and returns only Oskari style's featureStyle
    public JSONObject getOskariStyle () {
        if (customStyle != null) {
            return customStyle;
        }
        if (getProcessor().isPresent()) {
            return getProcessor().get().getWFSLayerOptions(layerId).getDefaultFeatureStyle();
        }
        return getVectorStyleService().getOskariFeatureStyle(style);
    }
}
