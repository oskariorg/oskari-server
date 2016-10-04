package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.PointTransformer;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Transforms coordinates from projection to another. Transformation class can be configured
 * with property 'projection.library.class' (defaults to fi.nls.oskari.map.geometry.ProjectionHelper).
 *
 * Takes 'lan', 'lot', 'srs' and 'targetSRS' parameters and returns a JSONObject with transformed result:
 * {
 *     lan: 123,
 *     lot : 456,
 *     srs : "EPSG:789"
 * }
 */
@OskariActionRoute("Coordinates")
public class CoordinatesHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(CoordinatesHandler.class);
    private static final String PROP_LIBRARY_CLASS = "projection.library.class";

    static final String TARGET_SRS = "targetSRS";
    private static final String PROPERTY_FORCEXY = "org.geotools.referencing.forceXY";

    private PointTransformer service = null;

    @Override
    public void init() {
        super.init();
        final String className = PropertyUtil.getOptional(PROP_LIBRARY_CLASS);
        if(className == null) {
            return;
        }
        try {
            final Class clazz = Class.forName(className);
            service = (PointTransformer) clazz.newInstance();
        } catch (Exception e) {
            LOG.error(e, "Error initalizing projection library for classname:", PROP_LIBRARY_CLASS,
                    " - Make sure it's available in the classpath.");
        }
    }

    @Override
    public void handleAction(final ActionParameters params)
            throws ActionException {

        final Point point = new Point(params.getRequiredParamDouble(PARAM_LON),
                params.getRequiredParamDouble(PARAM_LAT));
        final String srs = params.getRequiredParam(PARAM_SRS);
        final String target = params.getRequiredParam(TARGET_SRS);

        LOG.debug("Params - lon", point.getLon(), "lat", point.getLat(), "in", srs);
        try {
            boolean lenient = false;
            CoordinateReferenceSystem sourceCrs = CRS.decode(srs);
            CoordinateReferenceSystem targetCrs = CRS.decode(target);

            //seems, that at least 4326 -> 3067 doesn't work if we provide lon / lat in correct order. swap. *sigh*
            if ("EPSG:4326".equals(srs)) {
                double lon = point.getLon();
                double lat = point.getLat();
                point.setLat(lon);
                point.setLon(lat);
            }

            MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);
            DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getLon(), point.getLat());
            DirectPosition2D destDirectPosition2D = new DirectPosition2D(targetCrs);
            mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
            Point value = null;

            //We probably wanna swap lonlat order if the source's axis order differs from target's.
            //Unless when transforming from 4326. *sigh*
            if (!"EPSG:4326".equals(srs)) {
                if (ProjectionHelper.isFirstAxisNorth(sourceCrs) != ProjectionHelper.isFirstAxisNorth(targetCrs)) {
                    value = new Point(destDirectPosition2D.y, destDirectPosition2D.x);
                } else {
                    value = new Point(destDirectPosition2D.x, destDirectPosition2D.y);
                }
            } else {
                value = new Point(destDirectPosition2D.x, destDirectPosition2D.y);
            }

            LOG.debug("Reprojected - lon", value.getLon(), "lat", value.getLat(), "in", target);
            JSONObject response = new JSONObject();
            JSONHelper.putValue(response, PARAM_LON, value.getLon());
            JSONHelper.putValue(response, PARAM_LAT, value.getLat());
            JSONHelper.putValue(response, PARAM_SRS, target);
            ResponseHelper.writeResponse(params, response);
        } catch (RuntimeException ex) {
            throw new ActionParamsException(ex.getMessage());
        }

        catch (NoSuchAuthorityCodeException ex) {
            throw new ActionParamsException(ex.getMessage());
        } catch (FactoryException ex) {
            throw new ActionParamsException(ex.getMessage());
        } catch (TransformException e) {
            e.printStackTrace();
        }

    }

    private PointTransformer getTransformer() {
        if(service == null) {
            service = new ProjectionHelper();
        }
        return service;
    }
}
