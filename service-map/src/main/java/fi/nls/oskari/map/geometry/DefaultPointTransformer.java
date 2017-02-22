package fi.nls.oskari.map.geometry;

import fi.nls.oskari.domain.geo.Point;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Wraps some
 */
public class DefaultPointTransformer implements PointTransformer {
    private final ProjectionHelper service = new ProjectionHelper();

    public Point reproject(final Point point, final String src, final String target) {
        try {
            CoordinateReferenceSystem sourceCrs = CRS.decode(src);
            CoordinateReferenceSystem targetCrs = CRS.decode(target);
            Point result = service.transformPoint(point, sourceCrs, targetCrs);
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

}
