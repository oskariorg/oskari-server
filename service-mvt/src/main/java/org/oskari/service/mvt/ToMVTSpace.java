package org.oskari.service.mvt;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.GeometryEditor.GeometryEditorOperation;

/**
 * This translates the real-world coordinates of a geometry to the inner
 * virtual-coordinate space of the tile. Inside the tile a coordinate consists
 * of two integers (x,y) both in the range of [0,extent] (usually extent=4096).
 * 
 * As a result of this snapping to integer grid, vertices of the original
 * geometry might snap to the same point thereby creating duplicate points,
 * which we don't want. Those are removed. This can make Points in MultiPoint
 * disappear, LineStrings to deprecate to a single Point and LinearRings to collapse.
 * 
 * In case the generated geometry is not valid null is returned.
 * For example when LineString deprecates to a Single Point, or Polygons exterior
 * ring collapses.
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
        // This function is called by GeometryEditor which handles all other cases

        if (geometry instanceof LinearRing) {
            return edit((LinearRing) geometry, factory);
        }

        if (geometry instanceof LineString) {
            return edit((LineString) geometry, factory);
        }

        if (geometry instanceof Point) {
            Coordinate c = ((Point) geometry).getCoordinate();
            return factory.createPoint(translateAndScale(c));
        }

        return geometry;
    }

    private LinearRing edit(LinearRing linearRing, GeometryFactory factory) {
        Coordinate[] coords = linearRing.getCoordinates();
        Coordinate[] edited = translateAndScale(coords);
        edited = removeDuplicates(edited);
        if (edited.length < 4) {
            return null;
        }
        return factory.createLinearRing(edited);
    }

    private LineString edit(LineString lineString, GeometryFactory factory) {
        Coordinate[] coords = lineString.getCoordinates();
        Coordinate[] edited = translateAndScale(coords);
        edited = removeDuplicates(edited);
        if (edited.length < 2) {
            return null;
        }
        return factory.createLineString(edited);
    }

    private Coordinate[] translateAndScale(Coordinate[] coords) {
        Coordinate[] edited = new Coordinate[coords.length];
        for (int i = 0; i < coords.length; i++) {
            edited[i] = translateAndScale(coords[i]);
        }
        return edited;
    }

    private Coordinate translateAndScale(Coordinate c) {
        double x = Math.round(sx * (c.x - tx));
        double y = Math.round(sy * (c.y - ty));
        return new Coordinate(x, y);
    }

    private Coordinate[] removeDuplicates(Coordinate[] coords) {
        int n = 1;
        Coordinate prev = coords[0];
        for (int i = 1; i < coords.length; i++) {
            Coordinate c = coords[i];
            if (prev.x != c.x || prev.y != c.y) {
                n++;
                prev = c;
            }
        }
        if (n == coords.length) {
            return coords;
        }
        Coordinate[] noDuplicates = new Coordinate[n];
        prev = coords[0];
        noDuplicates[0] = prev;
        n = 1;
        for (int i = 1; i < coords.length; i++) {
            Coordinate c = coords[i];
            if (prev.x != c.x || prev.y != c.y) {
                noDuplicates[n++] = c;
                prev = c;
            }
        }
        return noDuplicates;
    }

}
