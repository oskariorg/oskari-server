package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
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
     * @return projected geometry as wkt
     */
    public static String transformLayerCoverage(final String wkt, final String targetSRS) {
        GeometryFactory gf = new GeometryFactory();
        Geometry geom = parseWKT(wkt, gf);
        if (geom == null) {
            return null;
        }
        CoordinateSequence cs = interpolateLinear(((Polygon) geom).getExteriorRing(), 1.0, gf);
        geom = gf.createPolygon(cs);
        // input axis orientation is / must be x=lon y=lat
        CoordinateReferenceSystem targetCrs = getCRS(targetSRS);
        final Geometry transformed = transform(geom, CRS_EPSG_4326, targetCrs);
        // output is x=lon y=lat always in every projection
        return getWKT(transformed);
    }

    protected static CoordinateSequence interpolateLinear(LineString line, double threshhold, GeometryFactory gf) {
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
            if (dy == 0 && dx == 0) {
                continue;
            }
            if (dy == 0) {
                int nSeg = (int) Math.ceil(Math.abs((dx / threshhold)));
                for (int j = 0; j < nSeg - 1; j++) {
                    if (i == tempPointArray.length) {
                        tempPointArray = grow(tempPointArray);
                    }
                    // Multiply each time to avoid snowballing a possible rounding error of dx / nSeg
                    tempPointArray[i++] = x0 + (j + 1) * dx / nSeg;
                    tempPointArray[i++] = y0;
                }
            } else if (dx == 0) {
                int nSeg = (int) Math.ceil(Math.abs((dy / threshhold)));
                for (int j = 0; j < nSeg - 1; j++) {
                    if (i == tempPointArray.length) {
                        tempPointArray = grow(tempPointArray);
                    }
                    tempPointArray[i++] = x0;
                    tempPointArray[i++] = y0 + (j + 1) * dy / nSeg;
                }
            } else {
                double c = Math.sqrt(dx * dx + dy * dy);
                int nSeg = (int) Math.ceil(Math.abs((c / threshhold)));
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
