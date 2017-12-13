package fi.nls.oskari.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;
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

public class GeoJSONReader {

    private static final GeometryFactory GF = new GeometryFactory();

    private static final String GEOMETRY = "geometry";
    private static final String TYPE = "type";
    private static final String COORDINATES = "coordinates";

    public static Geometry toGeometry(JSONObject geometry) throws JSONException {
        String geomType = geometry.getString(TYPE);
        switch (geomType) {
        case "Point":
            return toPoint(geometry);
        case "LineString":
            return toLineString(geometry);
        case "Polygon":
            return toPolygon(geometry);
        case "MultiPoint":
            return toMultiPoint(geometry);
        case "MultiLineString":
            return toMultiLineString(geometry);
        case "MultiPolygon":
            return toMultiPolygon(geometry);
        case "GeometryCollection":
            return toGeometryCollection(geometry);
        default:
            throw new IllegalArgumentException("Invalid geometry type");
        }
    }

    public static Point toPoint(JSONObject geometry)
            throws JSONException {
        return GF.createPoint(toCoordinate(geometry.getJSONArray(COORDINATES)));
    }

    public static LineString toLineString(JSONObject geometry)
            throws JSONException {
        Coordinate[] coordinates = toCoordinates(geometry.getJSONArray(COORDINATES));
        return GF.createLineString(coordinates);
    }

    public static Polygon toPolygon(JSONObject geometry)
            throws JSONException {
        return toPolygon(geometry.getJSONArray(COORDINATES));
    }

    public static MultiPoint toMultiPoint(JSONObject geometry)
            throws JSONException {
        Coordinate[] coordinates = toCoordinates(geometry.getJSONArray(COORDINATES));
        return GF.createMultiPoint(coordinates);
    }

    public static MultiLineString toMultiLineString(JSONObject geometry)
            throws JSONException {
        Coordinate[][] coordinates = toCoordinatesArray(geometry.getJSONArray(COORDINATES));
        int n = coordinates.length;
        LineString[] lineStrings = new LineString[n];
        for (int i = 0; i < n; i++) {
            lineStrings[i] = GF.createLineString(coordinates[i]);
        }
        return GF.createMultiLineString(lineStrings);
    }

    public static MultiPolygon toMultiPolygon(JSONObject geometry)
            throws JSONException {
        JSONArray arrayOfPolygons = geometry.getJSONArray(COORDINATES);
        int n = arrayOfPolygons.length();
        Polygon[] polygons = new Polygon[n];
        for (int i = 0; i < n; i++) {
            polygons[i] = toPolygon(arrayOfPolygons.getJSONArray(i));
        }
        return GF.createMultiPolygon(polygons);
    }

    public static GeometryCollection toGeometryCollection(JSONObject geometry)
            throws JSONException {
        JSONArray geometryArray = geometry.getJSONArray(GEOMETRY);
        int n = geometryArray.length();
        Geometry[] geometries = new Geometry[n];
        for (int i = 0; i < n; i++) {
            geometries[i] = toGeometry(geometryArray.getJSONObject(i));
        }
        return GF.createGeometryCollection(geometries);
    }

    private static Coordinate toCoordinate(JSONArray coordinate)
            throws JSONException {
        return new Coordinate(coordinate.getDouble(0), coordinate.getDouble(1));
    }

    private static Coordinate[] toCoordinates(JSONArray arrayOfCoordinates)
            throws JSONException {
        int n = arrayOfCoordinates.length();
        Coordinate[] coordinates = new Coordinate[n];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinate(arrayOfCoordinates.getJSONArray(i));
        }
        return coordinates;
    }

    private static Coordinate[][] toCoordinatesArray(JSONArray arrayOfArrayOfCoordinates)
            throws JSONException {
        int n = arrayOfArrayOfCoordinates.length();
        Coordinate[][] coordinates = new Coordinate[n][];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinates(arrayOfArrayOfCoordinates.getJSONArray(i));
        }
        return coordinates;
    }

    private static Polygon toPolygon(JSONArray coordinatesArray)
            throws JSONException {
        Coordinate[][] coordinates = toCoordinatesArray(coordinatesArray);
        LinearRing exterior = GF.createLinearRing(coordinates[0]);
        LinearRing[] interiors = new LinearRing[coordinates.length - 1];
        for (int i = 1; i < coordinates.length; i++) {
            interiors[i - 1] = GF.createLinearRing(coordinates[i]);
        }
        return GF.createPolygon(exterior, interiors);
    }
}
