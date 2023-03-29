package fi.nls.oskari.map.style;

import java.util.List;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.service.OskariComponent;

public abstract class VectorStyleService extends OskariComponent  {
    public abstract VectorStyle getDefaultStyle();
    public abstract VectorStyle getStyleById(final long id);
    public abstract List<VectorStyle> getStylesByUser (final long user);
    public abstract List<VectorStyle> getStyles (final long userId, final int layerId);
    public abstract boolean hasPermissionToAlter(final long id, final User user);
    public abstract long deleteStyle(final long id);
    public abstract long saveStyle(final VectorStyle style);
    public abstract long updateStyle(final VectorStyle style);
    public abstract List<VectorStyle> getAdminStyles (final int layerId);
    public abstract long deleteAdminStyle(final long id);
    public abstract long saveAdminStyle(final VectorStyle style);
    public abstract long updateAdminStyle(final VectorStyle style);
}
