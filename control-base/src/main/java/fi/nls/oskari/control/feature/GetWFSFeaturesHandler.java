package fi.nls.oskari.control.feature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.util.ServiceFactory;
import org.oskari.service.wfs.client.CoordinateTransformer;
import org.oskari.service.wfs.client.OskariWFS110Client;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetWFSFeatures")
public class GetWFSFeaturesHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetWFSFeaturesHandler.class);

    protected static final String ERR_LAYER_TYPE_NOT_WFS = "Specified layer is not a WFS layer";
    protected static final String ERR_BBOX_INVALID = "Invalid bbox";
    protected static final String ERR_BBOX_OUT_OF_CRS = "bbox not within CRS extent";
    protected static final String ERR_CRS_DECODE_FAIL = "Failed to decode CRS";
    protected static final String ERR_TRANSFORM_FIND_FAIL = "Failed to find CRS transformation";
    protected static final String ERR_REPOJECTION_FAIL = "Reprojection failed";
    protected static final String ERR_GEOJSON_ENCODE_FAIL = "Failed to write GeoJSON";

    private static final String GEOJSON_CONTENT_TYPE = "application/vnd.geo+json";
    private static final String PROPERTY_NATIVE_SRS = "oskari.native.srs";
    private static final String PARAM_BBOX = "bbox";
    private static final FeatureJSON FJ = new FeatureJSON();
    private static final byte[] EMPTY_GEOJSON_FEATURE_COLLECTION = "{\"type\": \"FeatureCollection\", \"features\": []}".getBytes(StandardCharsets.UTF_8);

    private PermissionHelper permissionHelper;
    private OskariWFS110Client wfsClient;

    private CoordinateReferenceSystem nativeCRS;
    private CoordinateReferenceSystem webMercator;

    protected void setPermissionHelper(PermissionHelper permissionHelper) {
        this.permissionHelper = permissionHelper;
    }

    protected void setWFSClient(OskariWFS110Client wfsClient) {
        this.wfsClient = wfsClient;
    }

    @Override
    public void init() {
        if (permissionHelper == null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    ServiceFactory.getPermissionsService());
        };
        if (wfsClient == null) {
            wfsClient = new OskariWFS110Client();
        }
    }

    private CoordinateReferenceSystem getNativeCRS() {
        if (nativeCRS == null) {
            try {
                String nativeSrs = PropertyUtil.get(PROPERTY_NATIVE_SRS, "EPSG:4326");
                nativeCRS = CRS.decode(nativeSrs, true);
            } catch (Exception e) {
                LOG.error(e, "Failed to decode Web Mercator CRS!");
            }
        }
        return nativeCRS;
    }

    private CoordinateReferenceSystem getWebMercator() {
        if (webMercator == null) {
            try {
                webMercator = CRS.decode("EPSG:3857", true);
            } catch (Exception e) {
                LOG.error(e, "Failed to decode Web Mercator CRS!");
            }
        }
        return webMercator;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        int layerId = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        String bboxStr = params.getRequiredParam(PARAM_BBOX);
        OskariLayer layer = findLayer(layerId, params.getUser());

        CoordinateReferenceSystem nativeCRS = getNativeCRS();
        CoordinateReferenceSystem targetCRS = getWebMercator(); // Only support WebMercator for now
        ReferencedEnvelope bbox = parseBbox(bboxStr, targetCRS);

        // TODO: Figure out if layer supports targetSrsName
        // If it does let the WFS service do the transformation
        SimpleFeatureCollection fc = getFeatures(layer, bbox, nativeCRS, targetCRS);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (fc.isEmpty()) {
                baos.write(EMPTY_GEOJSON_FEATURE_COLLECTION);
            } else {
                FJ.writeFeatureCollection(fc, baos);
            }
            ResponseHelper.writeResponse(params, 200, GEOJSON_CONTENT_TYPE, baos);
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

    private OskariLayer findLayer(int layerId, User user) throws ActionException {
        OskariLayer layer = permissionHelper.getLayer(layerId, user);
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
        }
        return layer;
    }

    protected SimpleFeatureCollection getFeatures(OskariLayer layer, ReferencedEnvelope bbox,
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

        SimpleFeatureCollection features = getFeatures(layer, requestBbox, nativeCRS);
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

    private SimpleFeatureCollection getFeatures(OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs) {
        String endPoint = layer.getUrl();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();
        // TODO: Figure out the maxFeatures from the layer
        int maxFeatures = 10000;
        return wfsClient.tryGetFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
    }

}
