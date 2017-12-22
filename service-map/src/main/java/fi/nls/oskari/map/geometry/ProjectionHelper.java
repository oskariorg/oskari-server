package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Helper for transformations
 */
public class ProjectionHelper implements PointTransformer {

    private static Logger log = LogFactory.getLogger(ProjectionHelper.class);
    private static String LONG_SRS_NAME_BASE = "urn:ogc:def:crs:EPSG::";

    public static Point transformPoint(final double lon, final double lat, final String sourceSRS, final String targetSRS) {
        return transformPoint(new Point(lon, lat), sourceSRS, targetSRS);
    }
    public Point reproject(final Point point, final String sourceSRS, final String targetSRS) {
        return transformPoint(point, sourceSRS, targetSRS);
    }
    public static Point transformPoint(final Point point, final String sourceSRS, final String targetSRS) {
        try {
            // use always lon coordinate 1st order
            CoordinateReferenceSystem sourceCrs = CRS.decode(sourceSRS, true);
            CoordinateReferenceSystem targetCrs = CRS.decode(targetSRS, true);
            return transformPoint(point, sourceCrs, targetCrs);

        } catch (Exception e) {
            log.error(e, "Transform CRS decoding failed! Params: sourceSRS", sourceSRS, "targetSRS", targetSRS, "Point", point );
        }
        return null;
    }

    public static Point transformPoint(final String lon, final String lat, final String sourceSRS, final String targetSRS) {
        try {
            double dlon = Double.parseDouble(lon);
            double dlat = Double.parseDouble(lat);
            return transformPoint(new Point(dlon, dlat), sourceSRS, targetSRS);

        } catch (Exception e) {
            log.error(e, "Transform parsing lon,lat double  failed! Params:: sourceSRS: ", sourceSRS, "targetSRS: ", targetSRS, "Point: ", lon, "  ", lat);
        }
        return null;
    }
    public static Point transformPoint(final double lon, final double lat, final CoordinateReferenceSystem sourceCrs, final String targetSRS) {
        try {
            // use always lon coordinate 1st order
            CoordinateReferenceSystem targetCrs = CRS.decode(targetSRS, true);
            return transformPoint(new Point(lon, lat), sourceCrs, targetCrs);

        } catch (Exception e) {
            log.error(e, "Transform CRS decoding failed! Params: targetSRS", targetSRS, "Point: ",lon,"  ",lat);
        }
        return null;
    }
    public static Point transformPoint(final double lon, final double lat, final CoordinateReferenceSystem sourceCrs, final CoordinateReferenceSystem targetCrs) {
        try {
            return transformPoint(new Point(lon, lat), sourceCrs, targetCrs);

        } catch (Exception e) {
            log.error(e, "Transform CRS decoding failed! Params: targetSRS", targetCrs, "Point: ",lon,"  ",lat);
        }
        return null;
    }

    /**
     * Transforms point coordinates to target projection
      * @param point is in source projection ( x/lon must be always to the east and y/lat is to the north)
     * @param sourceCrs
     * @param targetCrs
     * @return Point in target projection ( x/lon is always to the east and y/lat is to the north)
     */
    public static Point transformPoint(final Point point, final CoordinateReferenceSystem sourceCrs, final CoordinateReferenceSystem targetCrs) {
        try {

            if (sourceCrs.getName().equals(targetCrs.getName())) return point;
            // When using a CoordinateReferenceSystem that has been parsed from WKT you will often need to “relax” the accuracy
            // by setting the lenient parameter to true when searching with findMathTransform.
            boolean lenient = false;
            MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);
            DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getLon(), point.getLat());
            // Just in case that sourceCrs axis order is not forced as lon 1st
            if (isFirstAxisNorth(sourceCrs)) {
                // reverse xy lon 1st
                srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getLat(), point.getLon());
            }
            DirectPosition2D destDirectPosition2D = new DirectPosition2D(targetCrs);
            mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
            // Just in case that targetCrs axis order is not forced as lon 1st
            if (isFirstAxisNorth(targetCrs)) {
                // reverse xy lon 1st
                return new Point(destDirectPosition2D.y, destDirectPosition2D.x);
            }
            return new Point(destDirectPosition2D.x, destDirectPosition2D.y);
        } catch (Exception e) {
            log.error("Transform failed! Params: sourceSRS", sourceCrs.getName(), "targetSRS", targetCrs.getName(), "Point", point, "Msg:", e.getMessage());
        }
        return null;
    }

    public static boolean isFirstAxisNorth(CoordinateReferenceSystem crs) {
        return crs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.NORTH ||
                crs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.UP ||
                crs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.DISPLAY_UP;
    }

    /**
     * Return epsg short
     * urn:ogc:def:crs:EPSG::32635  --> EPSG:32635
     * @param crs
     * @return  epsg in short syntax
     */
    public static String shortSyntaxEpsg(String crs) {
        if (crs == null) {
            return null;
        }
        try {
            CoordinateReferenceSystem sourceCRS = CRS.decode(crs);
            crs = CRS.lookupIdentifier(sourceCRS, true);
            return crs;
        } catch (Exception e) {
            log.debug("EPSG geotools decoding failed", e);
        }
        // try own parsing
        String[] epsg = crs.toUpperCase().split("EPSG");
        if (epsg.length > 1) {
            String[] code = epsg[1].split(":");
            if (code.length > 2) {
                return "EPSG:" + code[2];
            }
        }
        return crs;
    }
    /**
     * Return epsg long
     * e.g. EPSG:3035  --> urn:ogc:def:crs:EPSG::3035
     * @param crs short syntax e.g. EPSG:3035
     * @return  epsg in long syntax
     */
    public static String longSyntaxEpsg(String crs) {
        if (crs == null) {
            return null;
        }
        try {
            String[] epsg = crs.toUpperCase().split(":");
            String code = epsg[epsg.length-1];
            return LONG_SRS_NAME_BASE + code;
        } catch (Exception e) {
            log.debug("EPSG geotools srsName decoding failed", e);
        }
        return crs;
    }

    /**
     * Return Crs with long epsg syntax

     * @param crs short syntax e.g. EPSG:32635
     * @return  crs in long syntax
     */
    public static CoordinateReferenceSystem longSyntaxCrs(String crs) {
        if (crs == null) {
            return null;
        }
        try {
            return CRS.decode(longSyntaxEpsg(crs), true);
        } catch (Exception e) {
            log.debug("EPSG geotools crs decoding failed - long crs name", e);
        }
        return null;
    }


    /**
     * Transforms geojson geometry coordinates
     * Axis order in geojson should be always longitude 1st (sourceLon1st=true, targetLon1st=true)
     * If the axis order is according to OGC standards in geojson, use sourceLon1st=false
     * If it is requested, that the axis order is according to OGC standards in the result geojson, use targetLon1st=false
     *
     * NOTE! The geometry is expected to be ONLY the GeoJSON "geometry" content. Not FeatureCollection or the single whole
     * feature.
     *
     * @param geometry geojson geometry to be transformed
     * @param sourceSRS
     * @param targetSRS
     * @param sourceLon1st  true, if eastern coordinate (longitude) is 1st  in geojson (should be always so)
     *                      false, if the axis order is according to OGC standards for sourceSRS in geojson
     *                      (northern coordinate (latitude) could be 1st)
     * @param targetLon1st  true, if eastern coordinate (longitude) is 1st  in the result geojson (should be always so)
     *                      false, if the axis order is according to OGC standards for targetSRS in result geojson
     *                      (northern coordinate (latitude) could be 1st)
     *
     * @return  transformed geojson
     */
    public static JSONObject transformGeometry(JSONObject geometry, final String sourceSRS, final String targetSRS, boolean sourceLon1st, boolean targetLon1st) {
        try {
            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceSRS, sourceLon1st);
            CoordinateReferenceSystem targetCRS = CRS.decode(targetSRS, targetLon1st);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            // Bug in geotools --> it put geojson srid value as z-value into coordinates
            // Workaround remove srid in geojson
            if(geometry.has("srid")){
                geometry.remove("srid");
            }
            // Transform coordinates
            GeometryJSON jsonReader = new GeometryJSON();
            String str = geometry.toString();
            InputStream gjstream = new ByteArrayInputStream(str.getBytes());
            Geometry sourceGeometry = jsonReader.read(gjstream);


            Geometry targetGeometry = JTS.transform(sourceGeometry, transform);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            jsonReader.write(targetGeometry, output);

            return JSONHelper.createJSONObject(new String(output.toByteArray(), "UTF-8"));

        } catch (Exception e) {
            log.error(e, "Transform geojson geometry failed! Params:: sourceSRS: ", sourceSRS, "targetSRS: ", targetSRS, "geometry: ", geometry.toString());
        }
        return null;
    }
    /**
     * Does the flipping of geometry object incase the axis is YX
     * We used CoordinateArraySequence to get coordinate Array
     * As per its documentation "In this implementation, Coordinates returned
     * by #toArray and #getCoordinate are live -- modifications to them are
     * actually changing the CoordinateSequence's underlying data."
     * @param fnG
     */
    public static void flipFeatureYX(Geometry fnG) {
        fnG.apply(new CoordinateSequenceFilter() {

            public boolean isGeometryChanged() {
                return true;
            }

            public boolean isDone() {
                return false;
            }

            public void filter(CoordinateSequence seq, int i) {
                double x = seq.getX(i);
                double y = seq.getY(i);
                seq.setOrdinate(i, 0, y);
                seq.setOrdinate(i, 1, x);
            }
        });
    }
    /**
     * Swap xy order in feature geometry of all features in featurecollection
     * @param featureCollection
     */
    public static void swapGeometryXY(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) {
        FeatureIterator<SimpleFeature> featuresIter =  featureCollection.features();
        try {
            while (featuresIter.hasNext()) {
                SimpleFeature feature = featuresIter.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                ProjectionHelper.flipFeatureYX(geom);
            }
        }
        finally {
            featuresIter.close();
        }
    }
}
