package fi.nls.oskari.map.style;

import java.util.List;

import org.oskari.user.User;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

public abstract class VectorStyleService extends OskariComponent  {
    private static final String KEY_FEATURE_STYLE = "featureStyle";
    private static final Logger log = LogFactory.getLogger(VectorStyleService.class);

    public abstract VectorStyle getDefaultStyle();
    public abstract VectorStyle getStyleById(final long id);
    public abstract List<VectorStyle> getStylesByUser (final long user);
    public abstract List<VectorStyle> getStyles (final long userId, final int layerId);
    public abstract boolean hasPermissionToAlter(final long id, final User user);
    public abstract void deleteStyle(final long id);
    public abstract long saveStyle(final VectorStyle style);
    public abstract long updateStyle(final VectorStyle style);
    public abstract List<VectorStyle> getAdminStyles (final int layerId);
    public abstract void deleteAdminStyle(final long id);
    public abstract long saveAdminStyle(final VectorStyle style);
    public abstract long updateAdminStyle(final VectorStyle style);

    public JSONObject getDefaultFeatureStyle () {
        VectorStyle defaultStyle = getDefaultStyle();
        if (defaultStyle == null) {
            log.info("Can't find default vector style from db, using default style from WFSLayerOptions");
            return WFSLayerOptions.getDefaultOskariStyle();
        }
        return JSONHelper.getJSONObject(defaultStyle.getStyle(), KEY_FEATURE_STYLE);
    }
    public JSONObject getOskariFeatureStyle(String name) {
        try {
            long styleId = Long.parseLong(name);
            return getOskariFeatureStyle(styleId);
        } catch (NumberFormatException ignored) {}
        return getDefaultFeatureStyle();
    }
    public JSONObject getOskariFeatureStyle(final long styleId) {
        VectorStyle style = getStyleById(styleId);
        if (style == null || !VectorStyle.TYPE_OSKARI.equals(style.getType())) {
            return getDefaultFeatureStyle();
        }
        JSONObject featureStyle = JSONHelper.getJSONObject(style.getStyle(), KEY_FEATURE_STYLE);
        return JSONHelper.merge(getDefaultFeatureStyle(), featureStyle);
    }
}
