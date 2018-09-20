package org.oskari.service.mvt;

import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.GeometryEditor.GeometryEditorOperation;

/**
 * Transform Geometry to Vector tile space
 * Also snaps to integer grid and removes duplicate points
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
            return factory.createLinearRing(edit(coords));
        }

        if (geometry instanceof LineString) {
            Coordinate[] coords = ((LineString) geometry).getCoordinates();
            return factory.createLineString(edit(coords));
        }

        if (geometry instanceof Point) {
            Coordinate c = ((Point) geometry).getCoordinate();
            int x = (int) Math.round(sx * (c.x - tx));
            int y = (int) Math.round(sy * (c.y - ty));
            return factory.createPoint(new Coordinate(x, y));
        }

        return geometry;
    }

    public Coordinate[] edit(Coordinate[] coords) {
        Coordinate[] a = new Coordinate[coords.length];

        Coordinate c = coords[0];
        int x = (int) Math.round(sx * (c.x - tx));
        int y = (int) Math.round(sy * (c.y - ty));
        a[0] = new Coordinate(x, y);
        int prevX = x;
        int prevY = y;

        int n = 1;
        for (int i = 1; i < coords.length; i++) {
            c = coords[i];
            x = (int) Math.round(sx * (c.x - tx));
            y = (int) Math.round(sy * (c.y - ty));
            if (x == prevX && y == prevY) {
                continue;
            }
            a[n++] = new Coordinate(x, y);
            prevX = x;
            prevY = y;
        }

        if (n == coords.length) {
            return a;
        }
        return Arrays.copyOf(a, n);
    }

}
