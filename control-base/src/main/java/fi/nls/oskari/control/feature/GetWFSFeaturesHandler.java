package fi.nls.oskari.control.feature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONUtil;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.wfs.client.OskariWFSClient;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionCommonException;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetWFSFeatures")
public class GetWFSFeaturesHandler extends AbstractWFSFeaturesHandler {

    protected static final String ERR_BBOX_INVALID = "Invalid bbox";
    protected static final String ERR_GEOJSON_ENCODE_FAIL = "Failed to write GeoJSON";
    protected static final String HEADER_HAS_MORE_FEATURES = "X-Maybe-Has-More-Features";

    private static final String PARAM_BBOX = "bbox";

    private static final String GEOJSON_CONTENT_TYPE = "application/vnd.geo+json; charset=utf-8";
    private static final byte[] EMPTY_GEOJSON_FEATURE_COLLECTION =
            "{\"type\": \"FeatureCollection\", \"features\": []}".getBytes(StandardCharsets.UTF_8);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        String id = params.getRequiredParam(ActionConstants.PARAM_ID);
        String bboxStr = params.getRequiredParam(PARAM_BBOX);

        Optional<UserLayerService> contentProcessor = getUserContentProsessor(id);
        OskariLayer layer = findLayer(id, params.getUser(), contentProcessor);

        String targetSRS = params.getHttpParam(ActionConstants.PARAM_SRS, "EPSG:3857");
        CoordinateReferenceSystem targetCRS;
        try {
            targetCRS = CRS.decode(targetSRS, true);
        } catch (Exception e) {
            throw new ActionParamsException("Invalid " + ActionConstants.PARAM_SRS);
        }

        ReferencedEnvelope bbox = parseBbox(bboxStr, targetCRS);
        layerAccessHandlers.forEach(handler -> handler.handle(layer, params.getUser()));
        SimpleFeatureCollection fc = getFeatures(id, layer, bbox, targetCRS, contentProcessor);
        if (fc.isEmpty()) {
            ResponseHelper.writeResponse(params, 200,
                    GEOJSON_CONTENT_TYPE, EMPTY_GEOJSON_FEATURE_COLLECTION);
            return;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
            int decimals = GeoJSONUtil.getNumDecimals(ProjectionHelper.isUnitDegrees(targetCRS));
            new FeatureJSON(new GeometryJSON(decimals)).writeFeatureCollection(fc, writer);
            // Oskari-specific layers (userlayer/myplaces/myfeatures) don't apply maxFeatures limit
            int maxFeatures = contentProcessor.isPresent()
                ? Integer.MAX_VALUE
                : OskariWFSClient.getMaxFeatures(layer);
            // fc should be an in-memory collection by this point, so size() should be cheap
            if (fc.size() >= maxFeatures) {
                params.getResponse().setHeader(HEADER_HAS_MORE_FEATURES, "true");
            }
            ResponseHelper.writeResponse(params, 200, GEOJSON_CONTENT_TYPE, baos);
        } catch (IOException e) {
            throw new ActionCommonException(ERR_GEOJSON_ENCODE_FAIL, e);
        }
    }

    @Override
    protected OskariWFSClient createWFSClient() {
        // Override the default functionality, return non-caching OskariWFSClient
        return new OskariWFSClient();
    }

    protected ReferencedEnvelope parseBbox(String bbox, CoordinateReferenceSystem crs)
            throws ActionParamsException {
        String[] a = bbox.split(",", 4);
        if (a.length != 4) {
            throw new ActionParamsException(ERR_BBOX_INVALID);
        }
        try {
            double x1 = Double.parseDouble(a[0]);
            double y1 = Double.parseDouble(a[1]);
            double x2 = Double.parseDouble(a[2]);
            double y2 = Double.parseDouble(a[3]);
            return new ReferencedEnvelope(x1, x2, y1, y2, crs);
        } catch (NumberFormatException e) {
            throw new ActionParamsException(ERR_BBOX_INVALID);
        }
    }

    private SimpleFeatureCollection getFeatures(String id, OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem targetCRS, Optional<UserLayerService> contentProcessor) throws ActionException {
        try {
            return featureClient.getFeatures(id, layer, bbox, targetCRS, contentProcessor);
        } catch (ServiceRuntimeException e) {
            throw new ActionCommonException(e.getMessage(), e);
        }
    }

}
