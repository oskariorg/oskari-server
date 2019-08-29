package org.oskari.print.request;

import java.util.Optional;
import org.json.JSONObject;
import org.oskari.service.user.UserLayerService;
import org.oskari.print.util.StyleUtil;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.domain.map.OskariLayer;

public class PrintLayer {

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

    public JSONObject getOskariStyle () {
        if (customStyle != null) {
            return customStyle;
        }
        if (getProcessor().isPresent()){
            return processor.get().getOskariStyle(layerId);
        }
        JSONObject defaultStyle = StyleUtil.getDefaultOskariStyle();
        if (StyleUtil.OSKARI_DEFAULT.equals(style)){
            return defaultStyle;
        }
        JSONObject options = oskariLayer.getOptions();
        JSONObject styles = JSONHelper.getJSONObject(options, StyleUtil.STYLES_JSON_KEY);
        if (styles == null) {
            return defaultStyle;
        }
        if (styles.has(style)){
            JSONObject namedStyle = JSONHelper.getJSONObject(
                    JSONHelper.getJSONObject(
                            JSONHelper.getJSONObject(styles, style),
                            getOskariLayer().getName()),
                    "featureStyle");
            return JSONHelper.merge(defaultStyle, namedStyle);
        }
        return defaultStyle;
    }

}
