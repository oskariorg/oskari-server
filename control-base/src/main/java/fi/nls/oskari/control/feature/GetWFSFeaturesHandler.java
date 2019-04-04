package fi.nls.oskari.control.feature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs3.CoordinateTransformer;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetWFSFeatures")
public class GetWFSFeaturesHandler extends AbstractWFSFeaturesHandler {

    protected static final String ERR_NATIVE_CRS_UNAVAILABLE = "Failed to find system native CRS";
    protected static final String ERR_BBOX_INVALID = "Invalid bbox";
    protected static final String ERR_BBOX_OUT_OF_CRS = "bbox not within CRS extent";
    protected static final String ERR_REPOJECTION_FAIL = "Reprojection failed";
    protected static final String ERR_GEOJSON_ENCODE_FAIL = "Failed to write GeoJSON";

    private static final String GEOJSON_CONTENT_TYPE = "application/vnd.geo+json";
    private static final String PARAM_BBOX = "bbox";
    private static final byte[] EMPTY_GEOJSON_FEATURE_COLLECTION = "{\"type\": \"FeatureCollection\", \"features\": []}".getBytes(StandardCharsets.UTF_8);

    private static final int NUM_DECIMAL_PLACES_DEGREE = 7; // For WGS84: 11.132mm precision at equator, more precise elsewhere, max error 5.5mm  
    private static final int NUM_DECIMAL_PLACES_OTHER = 2; // For metric projections: 10mm precision, max error 5mm

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        CoordinateReferenceSystem nativeCRS = getNativeCRS();
        if (nativeCRS == null) {
            throw new ActionException(ERR_NATIVE_CRS_UNAVAILABLE);
        }

        String id = params.getRequiredParam(ActionConstants.PARAM_ID);
        String bboxStr = params.getRequiredParam(PARAM_BBOX);
        OskariLayer layer = findLayer(id, params.getUser());
        String uuid = params.getUser().getUuid();

        String targetSRS = params.getHttpParam(ActionConstants.PARAM_SRS, "EPSG:3857");
        CoordinateReferenceSystem targetCRS;
        try {
            targetCRS = CRS.decode(targetSRS);
        } catch (Exception e) {
            throw new ActionParamsException("Invalid " + ActionConstants.PARAM_SRS);
        }

        // TODO: Figure out if layer supports targetSrsName
        // If it does let the WFS service do the transformation
        ReferencedEnvelope bbox = parseBbox(bboxStr, targetCRS);
        SimpleFeatureCollection fc = getFeatures(id, uuid, layer, bbox, nativeCRS, targetCRS);
        try {
            if (fc.isEmpty()) {
                ResponseHelper.writeResponse(params, 200, GEOJSON_CONTENT_TYPE, EMPTY_GEOJSON_FEATURE_COLLECTION);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int decimals = getNumDecimals(targetCRS);
                new FeatureJSON(new GeometryJSON(decimals)).writeFeatureCollection(fc, baos);
                ResponseHelper.writeResponse(params, 200, GEOJSON_CONTENT_TYPE, baos);
            }
        } catch (IOException e) {
            ResponseHelper.writeError(params, ERR_GEOJSON_ENCODE_FAIL);
        }
    }

    protected ReferencedEnvelope parseBbox(String bbox, CoordinateReferenceSystem crs) throws ActionParamsException {
        String[] a = bbox.split(",", 4);
        if (a.length != 4) {
            throw new ActionParamsException(ERR_BBOX_INVALID);
        }
        try {
            double x1 = Double.parseDouble(a[0]);
            double y1 = Double.parseDouble(a[1]);
            double x2 = Double.parseDouble(a[2]);
            double y2 = Double.parseDouble(a[3]);
            Envelope envelope = new Envelope(x1, x2, y1, y2);
            if (!isWithin(crs, envelope)) {
                throw new ActionParamsException(ERR_BBOX_OUT_OF_CRS);
            }
            return new ReferencedEnvelope(envelope, crs);
        } catch (NumberFormatException e) {
            throw new ActionParamsException(ERR_BBOX_INVALID);
        }
    }

    protected SimpleFeatureCollection getFeatures(String id, String uuid, OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem nativeCRS, CoordinateReferenceSystem targetCRS) throws ActionException {
        boolean needsTransform = !CRS.equalsIgnoreMetadata(nativeCRS, targetCRS);

        // Request features in nativeCRS (of the installation)
        // Most likely supported by all WFS layers
        ReferencedEnvelope requestBbox = bbox;
        if (needsTransform) {
            try {
                requestBbox = bbox.transform(nativeCRS, true);
            } catch (Exception e) {
                throw new ActionException(ERR_REPOJECTION_FAIL, e);
            }
        }
        // FIXME: getFeatures caches based on bbox -> skip caching here since we get the bbox from user!!
        SimpleFeatureCollection features = getFeatures(id, uuid, layer, requestBbox, nativeCRS);
        if (!needsTransform) {
            return features;
        }

        // Transform features to targetCRS
        try {
            CoordinateTransformer transformer = new CoordinateTransformer(nativeCRS, targetCRS);
            return transformer.transform(features);
        } catch (Exception e) {
            throw new ActionException(ERR_REPOJECTION_FAIL, e);
        }
    }

    private boolean isWithin(CoordinateReferenceSystem targetCRS, Envelope bbox) {
        org.opengis.geometry.Envelope targetCRSEnvelope = CRS.getEnvelope(targetCRS);
        DirectPosition lc = targetCRSEnvelope.getLowerCorner();
        if (bbox.getMinX() < lc.getOrdinate(0)) {
            return false;
        }
        if (bbox.getMinY() < lc.getOrdinate(1)) {
            return false;
        }
        DirectPosition uc = targetCRSEnvelope.getUpperCorner();
        if (bbox.getMaxX() > uc.getOrdinate(0)) {
            return false;
        }
        if (bbox.getMaxY() > uc.getOrdinate(1)) {
            return false;
        }
        return true;
    }

    /**
     * Get number of decimal places to use (maximum) when writing out the GeoJSON response.
     * The goal is to reduce the size of the actual response thereby reducing the amount
     * of memory and network used to serve the response while maintaining a precision that still far exceedes
     * the needs for our purposes
     *
     * @returns number of decimal places to use, the number depends on the unit of measure of the axes
     * of the coordinate system:
     * - NUM_DECIMAL_PLACES_DEGREE for degrees
     * - NUM_DECIMAL_PLACES_OTHER for others (metres, feet, what have you)
     */
    private int getNumDecimals(CoordinateReferenceSystem targetCRS) {
        boolean degrees = "°".equals(targetCRS.getCoordinateSystem().getAxis(0).getUnit().toString());
        return degrees ? NUM_DECIMAL_PLACES_DEGREE : NUM_DECIMAL_PLACES_OTHER;
    }

}
