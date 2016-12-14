package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.DefaultPointTransformer;
import fi.nls.oskari.map.geometry.PointTransformer;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

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
            PointTransformer transformer = getTransformer();
            Point value = srs.equals(target) ? point : transformer.reproject(point, srs, target);
            LOG.debug("Reprojected - lon", value.getLon(), "lat", value.getLat(), "in", target);
            JSONObject response = new JSONObject();
            JSONHelper.putValue(response, PARAM_LON, value.getLon());
            JSONHelper.putValue(response, PARAM_LAT, value.getLat());
            JSONHelper.putValue(response, PARAM_SRS, target);
            ResponseHelper.writeResponse(params, response);

        } catch (RuntimeException ex) {
            throw new ActionParamsException(ex.getMessage());
        }
    }

    private PointTransformer getTransformer() {
        if(service == null) {
            service = new DefaultPointTransformer();
        }
        return service;
    }
}
