package fi.nls.oskari.map.geometry;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;

/**
 * Helper for transformations
 */
public class ProjectionHelper {

    private static Logger log = LogFactory.getLogger(ProjectionHelper.class);

    public static Point transformPoint(final double x, final double y, final String sourceSRS, final String targetSRS) {
        return transformPoint(new Point(x, y), sourceSRS, targetSRS);
    }
    public static Point transformPoint(final Point point, final String sourceSRS, final String targetSRS) {
        try {
            CoordinateReferenceSystem sourceCrs = CRS.decode(sourceSRS);
            CoordinateReferenceSystem targetCrs = CRS.decode(targetSRS);
            return transformPoint(point, sourceCrs, targetCrs);

        } catch (Exception e) {
            log.error(e, "Transform CRS decoding failed! Params: sourceSRS", sourceSRS, "targetSRS", targetSRS, "Point", point );
        }
        return null;
    }
    public static Point transformPoint(final double x, final double y, final CoordinateReferenceSystem sourceCrs, final String targetSRS) {
        try {
            CoordinateReferenceSystem targetCrs = CRS.decode(targetSRS);
            return transformPoint(new Point(x, y), sourceCrs, targetCrs);

        } catch (Exception e) {
            log.error(e, "Transform CRS decoding failed! Params: targetSRS", targetSRS, "Point: ",x,"  ",y );
        }
        return null;
    }
    public static Point transformPoint(final Point point, final CoordinateReferenceSystem sourceCrs, final CoordinateReferenceSystem targetCrs) {
        try {

            boolean lenient = false;
            MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);

            //DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getY(), point.getX());
            DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getLon(), point.getLat());
            DirectPosition2D destDirectPosition2D = new DirectPosition2D();
            mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
            // Switch direction, if 1st coord is to the north on the other but not on the other
            if (isFirstAxisNorth(sourceCrs) != isFirstAxisNorth(targetCrs)) {
                return new Point(destDirectPosition2D.x, destDirectPosition2D.y);
            }
            return new Point(destDirectPosition2D.y, destDirectPosition2D.x);
        } catch (Exception e) {
            log.error(e, "Transform failed! Params: sourceSRS", sourceCrs, "targetSRS", targetCrs, "Point", point );
        }
        return null;
    }

    public static boolean isFirstAxisNorth(CoordinateReferenceSystem crs) {
        return crs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.NORTH ||
                crs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.UP ||
                crs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.DISPLAY_UP;
    }
}
