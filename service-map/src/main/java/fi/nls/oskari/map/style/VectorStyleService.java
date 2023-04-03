package fi.nls.oskari.map.style;

import java.util.List;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.style.VectorStyle;
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
    public abstract boolean hasPermissionToUpdate(final long id, final User user);
    public abstract long deleteStyle(final long id);
    public abstract long saveStyle(final VectorStyle style);
    public abstract long updateStyle(final VectorStyle style);
    public abstract List<VectorStyle> getAdminStyles (final int layerId);
    public abstract long deleteAdminStyle(final long id);
    public abstract long saveAdminStyle(final VectorStyle style);
    public abstract long updateAdminStyle(final VectorStyle style);

    public JSONObject getDefaultFeatureStyle () {
        VectorStyle defaultStyle = getDefaultStyle();
        if (defaultStyle == null) {
            log.error("Can't find default vector style");
            return new JSONObject();
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
