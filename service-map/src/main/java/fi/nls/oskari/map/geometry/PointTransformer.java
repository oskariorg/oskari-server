package fi.nls.oskari.map.geometry;

import fi.nls.oskari.domain.geo.Point;

/**
 * Created by SMAKINEN on 14.3.2016.
 */
public interface PointTransformer {
    Point reproject(final Point point, final String sourceSRS, final String targetSRS);
}
