package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class GeometryHelper {

    /**
     * Create a linear ring with 5 points from (x1,y1) (x2,y2) envelope
     */
    public static CoordinateSequence createLinearRing(GeometryFactory gf,
            double x1, double y1, double x2, double y2) {
        CoordinateSequence cs = gf.getCoordinateSequenceFactory().create(5, 2);
        cs.setOrdinate(0, 0, x1);
        cs.setOrdinate(0, 1, y1);
        cs.setOrdinate(1, 0, x2);
        cs.setOrdinate(1, 1, y1);
        cs.setOrdinate(2, 0, x2);
        cs.setOrdinate(2, 1, y2);
        cs.setOrdinate(3, 0, x1);
        cs.setOrdinate(3, 1, y2);
        cs.setOrdinate(4, 0, x1);
        cs.setOrdinate(4, 1, y1);
        return cs;
    }

    /**
     * Linear interpolation for a LineString
     * @param line
     *      LineString to interpolate
     * @param threshold
     *      if the distance between two consecutive points in the linestring is greater
     *      than this value new points will be added to the returned coordinate sequence
     * @param gf
     *      geometry factory to use, if it's null a new one will be constructed
     * @return
     *      sequence of coordinates that
     */
    public static CoordinateSequence interpolateLinear(LineString line, double threshold, GeometryFactory gf) {
        double[] tempPointArray = new double[128];
        int i = 0;

        Point point = line.getPointN(0);
        double x0 = point.getX();
        double y0 = point.getY();
        tempPointArray[i++] = x0;
        tempPointArray[i++] = y0;

        for (int p = 1; p < line.getNumPoints(); p++) {
            point = line.getPointN(p);
            double x1 = point.getX();
            double y1 = point.getY();
            double dx = x1 - x0;
            double dy = y1 - y0;
            if (dy == 0) {
                int nSeg = (int) Math.ceil(Math.abs(dx) / threshold);
                for (int j = 0; j < nSeg - 1; j++) {
                    if (i == tempPointArray.length) {
                        tempPointArray = grow(tempPointArray);
                    }
                    // Multiply each time to avoid snowballing a possible rounding error of dx / nSeg
                    tempPointArray[i++] = x0 + (j + 1) * dx / nSeg;
                    tempPointArray[i++] = y0;
                }
            } else if (dx == 0) {
                int nSeg = (int) Math.ceil(Math.abs(dy) / threshold);
                for (int j = 0; j < nSeg - 1; j++) {
                    if (i == tempPointArray.length) {
                        tempPointArray = grow(tempPointArray);
                    }
                    tempPointArray[i++] = x0;
                    tempPointArray[i++] = y0 + (j + 1) * dy / nSeg;
                }
            } else {
                double c = Math.sqrt(dx * dx + dy * dy);
                int nSeg = (int) Math.ceil(c / threshold);
                for (int j = 0; j < nSeg - 1; j++) {
                    if (i == tempPointArray.length) {
                        tempPointArray = grow(tempPointArray);
                    }
                    tempPointArray[i++] = x0 + (j + 1) * dx / nSeg;
                    tempPointArray[i++] = y0 + (j + 1) * dy / nSeg;
                }
            }
            if (i == tempPointArray.length) {
                tempPointArray = grow(tempPointArray);
            }
            tempPointArray[i++] = x1;
            tempPointArray[i++] = y1;
            x0 = x1;
            y0 = y1;
        }

        if (gf == null) {
            gf = new GeometryFactory();
        }
        CoordinateSequence cs = gf.getCoordinateSequenceFactory().create(i / 2, 2);
        for (int j = 0, k = 0; k < i; j++) {
            cs.setOrdinate(j, 0, tempPointArray[k++]);
            cs.setOrdinate(j, 1, tempPointArray[k++]);
        }
        return cs;
    }

    private static double[] grow(double[] arr) {
        int len = arr.length;
        double[] arr2 = new double[len * 2];
        System.arraycopy(arr, 0, arr2, 0, len);
        return arr2;
    }

    public static boolean isWithin(final CoordinateSequence cs,
            final double minX, final double minY,
            final double maxX, final double maxY) {
        if (maxX < minX) {
            throw new IllegalArgumentException("maxX < minX");
        }
        if (maxY < minY) {
            throw new IllegalArgumentException("maxY < minY");
        }
        for (int i = 0; i < cs.size(); i++) {
            Coordinate c = cs.getCoordinate(i);
            double x = c.x;
            double y = c.y;
            if (x < minX || x > maxX) {
                return false;
            }
            if (y < minY || y > maxY) {
                return false;
            }
        }
        return true;
    }

}
