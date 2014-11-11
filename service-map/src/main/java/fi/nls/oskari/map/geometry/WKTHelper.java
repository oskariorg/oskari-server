package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
    private static final Logger log = LogFactory.getLogger(WKTHelper.class);

    public final static String PROJ_EPSG_4326 = "EPSG:4326";
    public final static String PROJ_EPSG_3067 = "EPSG:3067";
    /**
     *
     * @param geometry original geometry
     * @param sourceSRS "EPSG:4326"
     * @param targetSRS "EPSG:3067"
     * @return projected geometry
     */
    public static Geometry transform(final Geometry geometry, final String sourceSRS, final String targetSRS){
        if(geometry == null || sourceSRS == null || targetSRS == null) {
            return null;
        }
        try {
            CoordinateReferenceSystem sourceCRS = getCRS(sourceSRS);
            CoordinateReferenceSystem targetCRS = getCRS(targetSRS);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
            return JTS.transform(geometry, transform);
        }
        catch (Exception ex) {
            log.error(ex, "Couldn't transform geometry to new projection");
        }
        return null;
    }
    /**
     *
     * @param wkt original geometry
     * @param sourceSRS "EPSG:4326"
     * @param targetSRS "EPSG:3067"
     * @return projected geometry as wkt
     */
    public static String transform(final String wkt, final String sourceSRS, final String targetSRS){
        final Geometry geom = parseWKT(wkt);
        if(geom == null) {
            return null;
        }
        final Geometry transformed = transform(geom, sourceSRS, targetSRS);
        return getWKT(transformed);
    }

    /**
     * Returns given geometry as a WKT String
     * @param geometry
     * @return
     */
    public static String getWKT(Geometry geometry){
        if(geometry == null) {
            return null;
        }
        final WKTWriter wrt = new WKTWriter();
        return wrt.write(geometry);
    }

    /**
     * Parses given WKT String to a Geometry object
     * @param wkt
     * @return geometry
     */
    public static Geometry parseWKT(final String wkt){
        final GeometryFactory geometryFactory = new GeometryFactory();
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
            return CRS.decode(srs);
        } catch (Exception e) {
            log.error(e, "CRS decoding failed");
        }
        return null;
    }
}
