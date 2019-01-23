package fi.nls.oskari.control.feature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.oskari.service.util.ServiceFactory;
import org.oskari.service.wfs.client.OskariWFS110Client;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetWFSFeatures")
public class GetWFSFeaturesHandler extends ActionHandler {

    protected static final String ERR_LAYER_TYPE_NOT_WFS = "Specified layer is not a WFS layer";
    protected static final String ERR_BBOX_INVALID = "Invalid bbox";
    protected static final String ERR_BBOX_OUT_OF_CRS = "bbox not within CRS extent";
    protected static final String ERR_CRS_DECODE_FAIL = "Failed to decode CRS";
    protected static final String ERR_TRANSFORM_FIND_FAIL = "Failed to find CRS transformation";
    protected static final String ERR_REPOJECTION_FAIL = "Repojection failed";

    private static final String GEOJSON_CONTENT_TYPE = "application/vnd.geo+json";
    private static final String PROPERTY_NATIVE_SRS = "oskari.native.srs";
    private static final String PARAM_BBOX = "bbox";
    private static final FeatureJSON FJ = new FeatureJSON();
    private static final byte[] EMPTY_GEOJSON_FEATURE_COLLECTION = "{\"type\": \"FeatureCollection\", \"features\": []}".getBytes(StandardCharsets.UTF_8);

    // Request Features in Oskari Native SRS
    private final String requestSrsName = PropertyUtil.get(PROPERTY_NATIVE_SRS, "EPSG:4326");

    private PermissionHelper permissionHelper;
    private OskariWFS110Client wfsClient;

    protected void setPermissionHelper(PermissionHelper permissionHelper) {
        this.permissionHelper = permissionHelper;
    }

    protected void setWFSClient(OskariWFS110Client wfsClient) {
        this.wfsClient = wfsClient;
    }

    @Override
    public void init() {
        if (permissionHelper != null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    ServiceFactory.getPermissionsService());
        };
        if (wfsClient != null) {
            wfsClient = new OskariWFS110Client();
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        int layerId = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        Envelope bbox = parseBbox(params.getRequiredParam(PARAM_BBOX));
        OskariLayer layer = findLayer(layerId, params.getUser());
        // Only support WebMercator for now
        // String targetSrs = params.getRequiredParam(ActionConstants.PARAM_SRS);
        String targetSrsName = "EPSG:3857";

        // TODO: Figure out if layer supports targetSrsName
        // If it does let the WFS service do the transformation
        SimpleFeatureCollection fc = getFeatures(layer, bbox, requestSrsName, targetSrsName);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (fc.isEmpty()) {
                baos.write(EMPTY_GEOJSON_FEATURE_COLLECTION);
            } else {
                FJ.writeFeatureCollection(fc, baos);
            }
            ResponseHelper.writeResponse(params, 200, GEOJSON_CONTENT_TYPE, baos);
        } catch (IOException e) {
            ResponseHelper.writeError(params, "Failed to write GeoJSON");
        }
    }

    private Envelope parseBbox(String bbox) throws ActionParamsException {
        String[] a = bbox.split(",", 4);
        if (a.length != 4) {
            throw new ActionParamsException(ERR_BBOX_INVALID);
        }
        try {
            return new Envelope(
                    Double.parseDouble(a[0]),  // x1
                    Double.parseDouble(a[2]),  // x2
                    Double.parseDouble(a[1]),  // y1
                    Double.parseDouble(a[3])); // y2
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

    protected SimpleFeatureCollection getFeatures(OskariLayer layer, Envelope bbox,
            String requestSrsName, String targetSrsName) throws ActionException {
        CoordinateReferenceSystem requestCRS;
        CoordinateReferenceSystem targetCRS;
        try {
            requestCRS = CRS.decode(requestSrsName, true);
            targetCRS = CRS.decode(targetSrsName, true);
        } catch (Exception e) {
            throw new ActionException(ERR_CRS_DECODE_FAIL);
        }

        if (!isWithin(targetCRS, bbox)) {
            throw new ActionParamsException(ERR_BBOX_OUT_OF_CRS);
        }

        boolean shouldTransform = !CRS.equalsIgnoreMetadata(requestCRS, targetCRS);

        MathTransform transformFromRequested;
        MathTransform transformToRequested;
        Envelope requestBbox;
        if (shouldTransform) {
            try {
                transformFromRequested = CRS.findMathTransform(requestCRS, targetCRS, true);
                transformToRequested = CRS.findMathTransform(targetCRS, requestCRS, true);
            } catch (Exception e) {
                throw new ActionException(ERR_TRANSFORM_FIND_FAIL);
            }
            try {
                requestBbox = JTS.transform(bbox, transformToRequested);
            } catch (Exception e) {
                throw new ActionException(ERR_REPOJECTION_FAIL);
            }
        } else {
            transformFromRequested = null;
            transformToRequested = null;
            requestBbox = bbox;
        }

        SimpleFeatureCollection features = getFeatures(layer, requestSrsName, requestBbox);
        if (shouldTransform) {
            try {
                features = transform(features, targetCRS, transformFromRequested);
            } catch (Exception e) {
                throw new ActionException(ERR_REPOJECTION_FAIL);
            }
        }
        return features;
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

    private SimpleFeatureCollection getFeatures(OskariLayer layer, String srsName, Envelope bbox) {
        String endPoint = layer.getUrl();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();
        // TODO: Figure out the maxFeatures from the layer 
        int maxFeatures = 10000;
        double[] box = { bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() };
        return wfsClient.tryGetFeatures(endPoint, user, pass, typeName, box, srsName, maxFeatures);
    }

    private SimpleFeatureCollection transform(SimpleFeatureCollection sfc, CoordinateReferenceSystem targetCRS, MathTransform transform)
            throws MismatchedDimensionException, TransformException {
        if (sfc.isEmpty()) {
            return sfc;
        }
        SimpleFeatureType newSchema = SimpleFeatureTypeBuilder.retype(sfc.getSchema(), targetCRS);
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(newSchema);
        DefaultFeatureCollection fc = new DefaultFeatureCollection(null, newSchema);
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                for (int i = 0; i < f.getAttributeCount(); i++) {
                    b.set(i, f.getAttribute(i));
                }
                SimpleFeature copy = b.buildFeature(f.getID());
                Object g = f.getDefaultGeometry();
                if (g != null) {
                    Geometry transformed = JTS.transform((Geometry) g, transform);
                    copy.setDefaultGeometry(transformed);
                }
                fc.add(copy);
            }
        }
        return fc;
    }

}
