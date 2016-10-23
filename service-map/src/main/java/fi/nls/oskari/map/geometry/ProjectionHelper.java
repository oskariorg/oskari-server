package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.factory.GeoTools;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;

import javax.naming.InitialContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper for transformations
 */
public class ProjectionHelper implements PointTransformer {

    private static Logger log = LogFactory.getLogger(ProjectionHelper.class);

    public static Point transformPoint(final double lon, final double lat, final String sourceSRS, final String targetSRS) {
        return transformPoint(new Point(lon, lat), sourceSRS, targetSRS);
    }
    public Point reproject(final Point point, final String sourceSRS, final String targetSRS) {
        return transformPoint(point, sourceSRS, targetSRS);
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
            CoordinateReferenceSystem targetCrs = CRS.decode(targetSRS);
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

    public static Point transformPoint(final Point point, final CoordinateReferenceSystem sourceCrs, final CoordinateReferenceSystem targetCrs) {
        try {

            if (sourceCrs.getName().equals(targetCrs.getName())) return point;
            // When using a CoordinateReferenceSystem that has been parsed from WKT you will often need to “relax” the accuracy
            // by setting the lenient parameter to true when searching with findMathTransform.
            boolean lenient = false;
            MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);

            DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getLon(), point.getLat());
            DirectPosition2D destDirectPosition2D = new DirectPosition2D(targetCrs);
            mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
            // Switch direction, if 1st coord is to the north on the other but not on the other
            if (isFirstAxisNorth(sourceCrs) != isFirstAxisNorth(targetCrs)) {
                return new Point(destDirectPosition2D.x, destDirectPosition2D.y);
            }
            return new Point(destDirectPosition2D.y, destDirectPosition2D.x);
        } catch (Exception e) {
            log.error(e, "Transform failed! Params: sourceSRS", sourceCrs, "targetSRS", targetCrs, "Point", point);
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
     * Transforms geojson geometry coordinates
     * Axis order in geojson should be always longitude 1st (sourceLon1st=true, targetLon1st=true)
     * If the axis order is according to OGC standards in geojson, use sourceLon1st=false
     * If it is requested, that the axis order is according to OGC standards in the result geojson, use targetLon1st=false
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

}
