package fi.nls.oskari.map.style;

import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.service.OskariComponent;

import java.util.List;

public abstract class VectorStyleService extends OskariComponent  {
    public abstract VectorStyle getStyleById(final long id);
    public abstract List<VectorStyle> getStylesByUser (final long user);
    public abstract List<VectorStyle> getStylesByLayerId (final int layerId);
    public abstract List<VectorStyle> getStyles (final long userId, final int layerId);
    public abstract long deleteStyle(final long id);
    public abstract long saveStyle(final VectorStyle style);
    public abstract long updateStyle(final VectorStyle style);
}
