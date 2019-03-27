package fi.nls.oskari.control.feature;

import fi.nls.oskari.control.*;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.util.ServiceFactory;
import org.oskari.service.wfs.client.CachingWFSClient;
import org.oskari.service.wfs3.CoordinateTransformer;


public abstract class AbstractWFSFeaturesHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(AbstractWFSFeaturesHandler.class);

    protected static final String ERR_LAYER_TYPE_NOT_WFS = "Specified layer is not a WFS layer";
    protected static final String ERR_REPOJECTION_FAIL = "Reprojection failed";
    private static final String PROPERTY_NATIVE_SRS = "oskari.native.srs";

    private PermissionHelper permissionHelper;
    private MyPlacesWFSHelper myPlacesHelper;
    private UserLayerWFSHelper userlayerHelper;

    private CoordinateReferenceSystem nativeCRS;
    private CachingWFSClient wfsClient;

    protected void setPermissionHelper(PermissionHelper permissionHelper) {
        this.permissionHelper = permissionHelper;
    }

    @Override
    public void init() {
        if (permissionHelper == null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    ServiceFactory.getPermissionsService());
        };
        if (myPlacesHelper == null) {
            myPlacesHelper = new MyPlacesWFSHelper();
        }
        if (userlayerHelper == null) {
            userlayerHelper = new UserLayerWFSHelper();
        }
        this.wfsClient = new CachingWFSClient();
    }

    @Override
    public abstract void handleAction(ActionParameters params) throws ActionException;

    protected CoordinateReferenceSystem getNativeCRS() {
        if (nativeCRS == null) {
            try {
                String nativeSrs = PropertyUtil.get(PROPERTY_NATIVE_SRS, "EPSG:4326");
                nativeCRS = CRS.decode(nativeSrs, true);
            } catch (Exception e) {
                LOG.error(e, "Failed to decode Native CRS!");
            }
        }
        return nativeCRS;
    }

    protected OskariLayer findLayer(String id, User user) throws ActionException {
        int layerId = getLayerId(id);
        OskariLayer layer = permissionHelper.getLayer(layerId, user);
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
        }
        return layer;
    }

    private int getLayerId(String id) throws ActionParamsException {
        if (myPlacesHelper.isMyPlacesLayer(id)) {
            return myPlacesHelper.getMyPlacesLayerId();
        }
        if (userlayerHelper.isUserlayerLayer(id)) {
            return userlayerHelper.getUserlayerLayerId();
        }
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new ActionParamsException("Invalid id");
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

    protected SimpleFeatureCollection getFeatures(String id, String uuid, OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs) {
        String endPoint = layer.getUrl();
        String version = layer.getVersion();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();
        // TODO: Figure out the maxFeatures from the layer
        int maxFeatures = 10000;

        Filter filter = null;
        if (myPlacesHelper.isMyPlacesLayer(layer)) {
            int categoryId = myPlacesHelper.getCategoryId(id);
            filter = myPlacesHelper.getFilter(categoryId, uuid, bbox);
        } else if (userlayerHelper.isUserlayerLayer(layer)) {
            int userlayerId = userlayerHelper.getUserlayerId(id);
            filter = userlayerHelper.getFilter(userlayerId, uuid, bbox);
        }

        SimpleFeatureCollection sfc = wfsClient.tryGetFeatures(endPoint, version, user, pass, typeName, bbox, crs, maxFeatures, filter);
        if (userlayerHelper.isUserlayerLayer(layer)) {
            try {
                sfc = userlayerHelper.retype(sfc);
            } catch (Exception e) {
                throw new ServiceRuntimeException("Failed to post-process user layer", e);
            }
        }
        return sfc;
    }

}
