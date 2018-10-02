package org.oskari.service.mvt;

import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.GeometryEditor.GeometryEditorOperation;

/**
 * Transform Geometry to Vector tile space
 * Also snaps to integer grid and removes duplicate points
 * 
 * If the resulting geometry would be invalid, null is returned
 */
public class ToMVTSpace implements GeometryEditorOperation {

    private final double tx;
    private final double ty;
    private final double sx;
    private final double sy;

    public ToMVTSpace(double tx, double ty, double sx, double sy) {
        this.tx = tx;
        this.ty = ty;
        this.sx = sx;
        this.sy = sy;
    }

    public Geometry edit(Geometry geometry, GeometryFactory factory) {
        if (geometry instanceof LinearRing) {
            Coordinate[] coords = ((LinearRing) geometry).getCoordinates();
            Coordinate[] edited = edit(coords, true, 4);
            if (edited == null) {
                // Too few points remaining
                return null;
            }
            return factory.createLinearRing(edited);
        }

        if (geometry instanceof LineString) {
            Coordinate[] coords = ((LineString) geometry).getCoordinates();
            Coordinate[] edited = edit(coords, false, 2);
            if (edited == null) {
                // Too few points remaining
                return null;
            }
            return factory.createLineString(edited);
        }
        
        if (geometry instanceof MultiPoint) {
            Coordinate[] points = ((MultiPoint) geometry).getCoordinates();
            Coordinate[] edited = edit(points, false, 0);
            if (edited == null) {
                return null;
            }
            if (edited.length == 1) {
                return factory.createPoint(edited[0]);
            }
            return factory.createMultiPoint(edited);
        }

        if (geometry instanceof Point) {
            Coordinate coord = ((Point) geometry).getCoordinate();
            int x = (int) Math.round(sx * (coord.x - tx));
            int y = (int) Math.round(sy * (coord.y - ty));
            return factory.createPoint(new Coordinate(x, y));
        }

        return geometry;
    }

    public Coordinate[] edit(Coordinate[] coords, boolean mustClose, int minNumPoints) {
        Coordinate[] edited = new Coordinate[coords.length];
        int n = 0;

        int x0 = Integer.MAX_VALUE;
        int y0 = Integer.MAX_VALUE;
        for (Coordinate coord : coords) {
            int x = (int) Math.round(sx * (coord.x - tx));
            int y = (int) Math.round(sy * (coord.y - ty));
            if (x == x0 && y == y0) {
                continue;
            }
            edited[n++] = new Coordinate(x, y);
            x0 = x;
            y0 = y;
        }

        if (n < minNumPoints) {
            return null;
        }
        if (n == edited.length) {
            return edited;
        }
        return Arrays.copyOf(edited, n);
    }

}
