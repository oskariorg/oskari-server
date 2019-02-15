package org.oskari.service.wfs.client.geojson;

import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Utility class for converting a Map<String, Object> presentation
 * of a GeoJSON geometry object to a JTS Geometry
 * 
 * This class is package private by design, the use case for this
 * is rather specific. You really shouldn't be using this class in any other context.
 */
class MapToGeoJSONGeometry {
    
    private static final GeometryFactory GF = new GeometryFactory();
    
    /**
     * Tries to convert a Map<String, Object> presentation of a GeoJSON
     * geometry object to a JTS Geometry.
     * @param maybeGeometry the possibly GeoJSON geometry
     * @return null if it can't be done, and the actual Geometry if everything is OK
     */
    static Geometry tryConvertToGeometry(Map<String, Object> maybeGeometry) {
        try {
            // Don't handle nulls in this context, try to avoid cluttering the code
            // If something is null along the way then an NPE is thrown
            // but those are handled by catching all exceptions
            String type = (String) maybeGeometry.get("type");
            switch (type) {
            case "Point":
                return toPoint(maybeGeometry);
            case "MultiPoint":
                return toMultiPoint(maybeGeometry);
            case "LineString":
                return toLineString(maybeGeometry);
            case "MultiLineString":
                return toMultiLineString(maybeGeometry);
            case "Polygon":
                return toPolygon(maybeGeometry);
            case "MultiPolygon":
                return toMultiPolygon(maybeGeometry);
            case "GeometryCollection":
                return toGeometryCollection(maybeGeometry);
            }
        } catch (Exception ignore) {
            // Something failed, probably a NPE somewhere along the way
            // But we don't really care _why_ it wasn't a proper GeoJSON geometry
            // the fact that it wasn't is good enough for us
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Point toPoint(Map<String, Object> maybeGeometry) {
        List<Object> coordinates = (List<Object>) maybeGeometry.get("coordinates");
        return GF.createPoint(toCoordinate(coordinates));
    }

    @SuppressWarnings("unchecked")
    private static MultiPoint toMultiPoint(Map<String, Object> maybeGeometry) {
        List<Object> coordinates = (List<Object>) maybeGeometry.get("coordinates");
        return GF.createMultiPoint(toCoordSeq(coordinates));
    }

    @SuppressWarnings("unchecked")
    private static LineString toLineString(Map<String, Object> maybeGeometry) {
        List<Object> coordinates = (List<Object>) maybeGeometry.get("coordinates");
        return GF.createLineString(toCoordSeq(coordinates));
    }

    @SuppressWarnings("unchecked")
    private static MultiLineString toMultiLineString(Map<String, Object> maybeGeometry) {
        List<Object> coordinates = (List<Object>) maybeGeometry.get("coordinates");
        LineString[] lineStrings = new LineString[coordinates.size()];
        for (int i = 0; i < lineStrings.length; i++) {
            CoordinateSequence seq = toCoordSeq((List<Object>) coordinates.get(i));
            lineStrings[i] = GF.createLineString(seq);
        }
        return GF.createMultiLineString(lineStrings);
    }

    @SuppressWarnings("unchecked")
    private static Polygon toPolygon(Map<String, Object> maybeGeometry) {
        List<Object> coordinates = (List<Object>) maybeGeometry.get("coordinates");
        return toPolygon(coordinates);
    }
    
    @SuppressWarnings("unchecked")
    private static Polygon toPolygon(List<Object> polygonCoordinates) {
        CoordinateSequence seq = toCoordSeq((List<Object>) polygonCoordinates.get(0));
        LinearRing exterior = GF.createLinearRing(seq);
        LinearRing[] interiors = new LinearRing[polygonCoordinates.size() - 1];
        for (int i = 1; i < polygonCoordinates.size(); i++) {
            seq = toCoordSeq((List<Object>) polygonCoordinates.get(0));
            interiors[i - 1] = GF.createLinearRing(seq);
        }
        return GF.createPolygon(exterior, interiors);
    }

    @SuppressWarnings("unchecked")
    private static MultiPolygon toMultiPolygon(Map<String, Object> maybeGeometry) {
        List<Object> coordinates = (List<Object>) maybeGeometry.get("coordinates");
        Polygon[] polygons = new Polygon[coordinates.size()];
        for (int i = 0; i < polygons.length; i++) {
            polygons[i] = toPolygon((List<Object>) coordinates.get(i));
        }
        return GF.createMultiPolygon(polygons);
    }

    @SuppressWarnings("unchecked")
    private static GeometryCollection toGeometryCollection(Map<String, Object> maybeGeometry) {
        List<Object> geometries = (List<Object>) maybeGeometry.get("geometries");
        Geometry[] geometriesArr = new Geometry[geometries.size()];
        for (int i = 0; i < geometriesArr.length; i++)  {
            Map<String, Object> maybeChildGeometry = (Map<String, Object>) geometries.get(i);
            Geometry childGeometry = tryConvertToGeometry(maybeChildGeometry);
            if (childGeometry == null) {
                return null;
            }
            geometriesArr[i] = childGeometry;
        }
        return GF.createGeometryCollection(geometriesArr);
    }
    
    @SuppressWarnings("unchecked")
    private static CoordinateSequence toCoordSeq(List<Object> list) {
        Coordinate[] arr = new Coordinate[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = toCoordinate((List<Object>) list.get(i));
        }
        // Determine the dimension based on the first entry
        // Does not verify that all of the coordinates are of the
        // same dimension
        int dimension = ((List<Object>) list.get(0)).size() > 2 ? 3 : 2;
        return new CoordinateArraySequence(arr, dimension);
    }

    private static Coordinate toCoordinate(List<Object> list) {
        double x = ((Number) list.get(0)).doubleValue();
        double y = ((Number) list.get(1)).doubleValue();
        double z = Coordinate.NULL_ORDINATE;
        if (list.size() > 2) {
            z = ((Number) list.get(2)).doubleValue(); 
        }
        return new Coordinate(x, y, z);
    }

}
