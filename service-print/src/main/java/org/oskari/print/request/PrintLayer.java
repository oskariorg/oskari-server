package org.oskari.print.request;

import java.util.Optional;

import org.oskari.service.user.UserLayerService;

import fi.nls.oskari.domain.map.OskariLayer;

public class PrintLayer {

    private final int zIndex;
    private OskariLayer oskariLayer;
    private String style;
    private int opacity;
    private Optional<UserLayerService> processor;
    private PrintTile[] tiles;

    public PrintLayer(int zIndex) {
        this.zIndex = zIndex;
    }

    public int getZIndex() {
        return zIndex;
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
        return processor;
    }

    public void setProcessor(Optional<UserLayerService> processor) {
        this.processor = processor;
    }

}
