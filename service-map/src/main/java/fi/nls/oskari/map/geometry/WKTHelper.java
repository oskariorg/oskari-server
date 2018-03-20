package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


/**
 * Simple helper methods to deal with WKT and projection transforms
 */
public class WKTHelper {
    public final static String PROJ_EPSG_4326 = "EPSG:4326";
    public final static String PROJ_EPSG_3067 = "EPSG:3067";
    private static final Logger log = LogFactory.getLogger(WKTHelper.class);
    public final static CoordinateReferenceSystem CRS_EPSG_4326 = getCRS(PROJ_EPSG_4326);

    private static final double INTERPOLATE_THRESHOLD = 1.0;
    private static final double WGS84_LON_MIN = -180.0;
    private static final double WGS84_LON_MAX=   180.0;
    private static final double WGS84_LAT_MIN =  -90.0;
    private static final double WGS84_LAT_MAX =   90.0;

    /**
     * @param geometry  original geometry
     * @param sourceSRS "EPSG:4326"
     * @param targetSRS "EPSG:3067"
     * @return projected geometry
     */
    public static Geometry transform(final Geometry geometry, final String sourceSRS, final String targetSRS) {
        if (geometry == null || sourceSRS == null || targetSRS == null) {
            return null;
        }
        try {
            CoordinateReferenceSystem sourceCRS = getCRS(sourceSRS);
            CoordinateReferenceSystem targetCRS = getCRS(targetSRS);
            return transform(geometry, sourceCRS, targetCRS);
        } catch (Exception ex) {
            log.error(ex, "Couldn't transform geometry to new projection");
        }
        return null;
    }

    public static Geometry transform(final Geometry geometry, final CoordinateReferenceSystem sourceCRS,
                                     final CoordinateReferenceSystem targetCRS) {

        if (geometry == null || sourceCRS == null || targetCRS == null) {
            return null;
        }
        try {
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            return JTS.transform(geometry, transform);
        } catch (Exception ex) {
            log.error(ex, "Couldn't transform geometry to new projection");
        }
        return null;
    }

    /**
     * @param wkt       original geometry
     * @param sourceSRS "EPSG:4326"
     * @param targetSRS "EPSG:3067"
     * @return projected geometry as wkt
     */
    public static String transform(final String wkt, final String sourceSRS, final String targetSRS) {
        final Geometry geom = parseWKT(wkt);
        if (geom == null) {
            return null;
        }
        // input axis orientation is / must be x=lon y=lat
        final Geometry transformed = transform(geom, sourceSRS, targetSRS);
        // output axis orientation is x=lon y=lat for every projections
        return getWKT(transformed);
    }

    /**
     * @param wkt       original geometry in EPSG:4326
     * @param targetSRS "EPSG:3067"
     * @return null if:
     *  - geometry is null
     *  - geometry is not a Polygon
     *  - any of the coordinates in the exterior ring is not within [-180,-90,180,90]
     *  otherwise return the exterior ring of the polygon projected to the targetSRS and
     *  possibly with extra interpolated points in-between of the original segments
     */
    public static String transformLayerCoverage(final String wkt, final String targetSRS) {
        GeometryFactory gf = new GeometryFactory();
        Geometry geom = parseWKT(wkt, gf);
        if (geom == null || !(geom instanceof Polygon)) {
            return null;
        }
        Polygon polygon = (Polygon) geom;
        LineString exterior = polygon.getExteriorRing();
        boolean withinWGS84Bounds = GeometryHelper.isWithin(exterior.getCoordinateSequence(),
                WGS84_LON_MIN, WGS84_LAT_MIN,
                WGS84_LON_MAX, WGS84_LAT_MAX);
        if (!withinWGS84Bounds) {
            log.info("Layer coverage not within WGS84 bounds, not interpolating or transforming extent");
            return null;
        }
        CoordinateSequence cs = GeometryHelper.interpolateLinear(exterior, INTERPOLATE_THRESHOLD, gf);
        polygon = gf.createPolygon(cs);
        // input axis orientation is / must be x=lon y=lat
        CoordinateReferenceSystem targetCrs = getCRS(targetSRS);
        final Geometry transformed = transform(polygon, CRS_EPSG_4326, targetCrs);
        // output is x=lon y=lat always in every projection
        return getWKT(transformed);
    }

    /**
     * Returns given geometry as a WKT String
     *
     * @param geometry
     * @return
     */
    public static String getWKT(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        final WKTWriter wrt = new WKTWriter();
        return wrt.write(geometry);
    }

    public static Geometry parseWKT(final String wkt) {
        return parseWKT(wkt, null);
    }

    /**
     * Parses given WKT String to a Geometry object
     *
     * @param wkt
     * @return geometry
     */
    public static Geometry parseWKT(final String wkt, GeometryFactory gf) {
        final GeometryFactory geometryFactory = gf != null ?  gf : new GeometryFactory();
        WKTReader parser = new WKTReader(geometryFactory);
        try {
            return parser.read(wkt);
        } catch (Exception e) {
            log.error(e, "Couldn't parse WKT to geometry:", wkt);
        }
        return null;
    }

    public static CoordinateReferenceSystem getCRS(final String srs) {
        try {
            return CRS.decode(srs,true);  // true --> lon always 1st
        } catch (Exception e) {
            log.error(e, "CRS decoding failed");
        }
        return null;
    }

    /**
     * Creates bbox for defined coordinates.
     *
     * @param westBoundLongitude
     * @param southBoundLatitude
     * @param eastBoundLongitude
     * @param northBoundLatitude
     * @return WKT String bbox
     */
    public static String getBBOX(final double westBoundLongitude, final double southBoundLatitude,
                                 final double eastBoundLongitude, final double northBoundLatitude) {
        String bbox = "POLYGON ((" + Double.toString(westBoundLongitude) + " " + Double.toString(southBoundLatitude) + ", " +
                Double.toString(westBoundLongitude) + " " + Double.toString(northBoundLatitude) + ", " +
                Double.toString(eastBoundLongitude) + " " + Double.toString(northBoundLatitude) + ", " +
                Double.toString(eastBoundLongitude) + " " + Double.toString(southBoundLatitude) + ", " +
                Double.toString(westBoundLongitude) + " " + Double.toString(southBoundLatitude) +
                "))";

        log.debug("BBOX: " + bbox);
        return bbox;
    }
}
