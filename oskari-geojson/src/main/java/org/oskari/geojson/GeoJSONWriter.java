package org.oskari.geojson;

import java.util.ArrayList;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Convert
 *  - GeoTools SimpleFeature(Collection)s
 *  - JTS geometries
 * to org.json.JSONObjects
 *
 * Single GeoJSONWriter is not threadsafe
 */
public class GeoJSONWriter {

    private final ArrayList<Double> arr2;
    private final ArrayList<Double> arr3;

    public GeoJSONWriter() {
        arr2 = new ArrayList<Double>(2);
        arr2.add(Double.NaN);
        arr2.add(Double.NaN);
        arr3 = new ArrayList<Double>(3);
        arr3.add(Double.NaN);
        arr3.add(Double.NaN);
        arr3.add(Double.NaN);
    }

    public JSONObject writeFeatureCollection(SimpleFeatureCollection fc)
            throws JSONException {
        JSONObject featureCollection = new JSONObject();
        featureCollection.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);

        JSONArray features = new JSONArray();
        featureCollection.put(GeoJSON.FEATURES, features);

        try (SimpleFeatureIterator it = fc.features()) {
            features.put(writeFeature(it.next()));
        }

        return featureCollection;
    }

    public JSONObject writeFeature(SimpleFeature f)
            throws JSONException {
        JSONObject feature = new JSONObject();
        feature.put(GeoJSON.TYPE, GeoJSON.FEATURE);

        GeometryAttribute ga = f.getDefaultGeometryProperty();
        Name gaName = null;
        if (ga != null) {
            gaName = ga.getName();
            Geometry geom = (Geometry) ga.getValue();
            if (geom != null) {
                feature.put(GeoJSON.GEOMETRY, writeGeometry(geom));
            }
        }

        JSONObject properties = null;
        for (Property p : f.getProperties()) {
            Name name = p.getName();
            if (name.equals(gaName)) {
                continue;
            }
            Object value = p.getValue();
            if (value == null) {
                continue;
            }
            if (properties == null) {
                properties = new JSONObject();
            }
            if (value instanceof Geometry) {
                properties.put(name.getLocalPart(), writeGeometry((Geometry) value));
            } else {
                properties.put(name.getLocalPart(), value);
            }
        }
        if (properties != null) {
            feature.put(GeoJSON.PROPERTIES, properties);
        }

        String id = f.getID();
        if (id != null && !id.isEmpty()) {
            feature.put(GeoJSON.ID, id);
        }

        return feature;
    }

    public JSONObject writeGeometry(Geometry geom)
            throws JSONException {
        if (geom instanceof Point) {
            return writePoint((Point) geom);
        } else if (geom instanceof LineString) {
            return writeLineString((LineString) geom);
        } else if (geom instanceof Polygon) {
            return writePolygon((Polygon) geom);
        } else if (geom instanceof MultiPoint) {
            return writeMultiPoint((MultiPoint) geom);
        } else if (geom instanceof MultiLineString) {
            return writeMultiLineString((MultiLineString) geom);
        } else if (geom instanceof MultiPolygon) {
            return writeMultiPolygon((MultiPolygon) geom);
        } else if (geom instanceof GeometryCollection) {
            return writeGeometryCollection((GeometryCollection) geom);
        } else {
            throw new IllegalArgumentException("Invalid geometry type");
        }
    }

    public JSONObject writePoint(Point geom)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put(GeoJSON.TYPE, GeoJSON.POINT);

        Coordinate c = geom.getCoordinate();
        json.put(GeoJSON.COORDINATES, writeCoordinate(c));
        return json;
    }

    public JSONObject writeLineString(LineString geom)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put(GeoJSON.TYPE, GeoJSON.LINESTRING);

        CoordinateSequence cs = geom.getCoordinateSequence();
        json.put(GeoJSON.COORDINATES, writeCoordinateSequence(cs));

        return json;
    }

    public JSONObject writePolygon(Polygon geom)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put(GeoJSON.TYPE, GeoJSON.POLYGON);
        
        json.put(GeoJSON.COORDINATES, writePolygonCoordinates(geom));
        return json;
    }
    
    private JSONArray writePolygonCoordinates(Polygon polygon) throws JSONException {
        JSONArray rings = new JSONArray();
        LineString ring = polygon.getExteriorRing();
        CoordinateSequence cs = ring.getCoordinateSequence();
        rings.put(writeCoordinateSequence(cs));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            ring = polygon.getInteriorRingN(i);
            cs = ring.getCoordinateSequence();
            rings.put(writeCoordinateSequence(cs));
        }
        return rings;
    }

    public JSONObject writeMultiPoint(MultiPoint geom)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put(GeoJSON.TYPE, GeoJSON.MULTI_POINT);

        JSONArray points = new JSONArray();
        json.put(GeoJSON.COORDINATES, points);

        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Point p = (Point) geom.getGeometryN(i);
            Coordinate c = p.getCoordinate();
            points.put(writeCoordinate(c));
        }

        return json;
    }

    public JSONObject writeMultiLineString(MultiLineString geom)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put(GeoJSON.TYPE, GeoJSON.MULTI_LINESTRING);

        JSONArray lineStrings = new JSONArray();
        json.put(GeoJSON.COORDINATES, lineStrings);

        for (int i = 0; i < geom.getNumGeometries(); i++) {
            LineString ls = (LineString) geom.getGeometryN(i);
            CoordinateSequence cs = ls.getCoordinateSequence();
            lineStrings.put(writeCoordinateSequence(cs));
        }

        return json;
    }

    public JSONObject writeMultiPolygon(MultiPolygon geom)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put(GeoJSON.TYPE, GeoJSON.MULTI_POLYGON);

        JSONArray polygons = new JSONArray();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            polygons.put(writePolygonCoordinates((Polygon) geom.getGeometryN(i)));
        }
        json.put(GeoJSON.COORDINATES, polygons);

        return json;
    }

    public JSONObject writeGeometryCollection(GeometryCollection geom)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put(GeoJSON.TYPE, GeoJSON.GEOMETRY_COLLECTION);

        JSONArray geometries = new JSONArray();
        json.put(GeoJSON.GEOMETRIES, geometries);

        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry g = geom.getGeometryN(i);
            geometries.put(writeGeometry(g));
        }

        return json;
    }

    private JSONArray writeCoordinate(Coordinate c)
            throws JSONException {
        final double x = c.x;
        final double y = c.y;
        final double z = c.z;
        if (Double.isNaN(z)) {
            // Re-use arr2 and arr3, will create more short-lived garbage,
            // but the ArrayLists within generated JSONArrays (which are expected
            // to live a bit longer) will be of optimal size (2 or 3 elements per
            // coordinate, rather than the JDK default 10).
            arr2.set(0, x);
            arr2.set(1, y);
            return new JSONArray(arr2);
        } else {
            arr3.set(0, x);
            arr3.set(1, y);
            arr3.set(2, z);
            return new JSONArray(arr3);
        }
    }

    private JSONArray writeCoordinateSequence(CoordinateSequence cs)
            throws JSONException {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < cs.size(); i++) {
            Coordinate c = cs.getCoordinate(i);
            arr.put(writeCoordinate(c));
        }
        return arr;
    }

}
