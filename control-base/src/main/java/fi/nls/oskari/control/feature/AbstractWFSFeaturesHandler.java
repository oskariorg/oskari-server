package fi.nls.oskari.control.feature;

import fi.nls.oskari.control.*;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.util.ServiceFactory;
import org.oskari.service.wfs.client.CachingWFSClient;
import org.oskari.service.wfs3.CoordinateTransformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


public abstract class AbstractWFSFeaturesHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(AbstractWFSFeaturesHandler.class);

    protected static final String ERR_LAYER_TYPE_NOT_WFS = "Specified layer is not a WFS layer";
    protected static final String ERR_REPOJECTION_FAIL = "Reprojection failed";
    private static final String PROPERTY_NATIVE_SRS = "oskari.native.srs";

    private PermissionHelper permissionHelper;

    private CoordinateReferenceSystem nativeCRS;
    private CachingWFSClient wfsClient;
    private Collection<UserLayerService> userContentProcessors = new ArrayList<>();

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
        this.wfsClient = new CachingWFSClient();
        // clear so running init() again doesn't accumulate processors
        userContentProcessors.clear();
        Map<String, UserLayerService> processorMap = OskariComponentManager.getComponentsOfType(UserLayerService.class);
        userContentProcessors.addAll(processorMap.values());
    }

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
        int userLayerId = userContentProcessors.stream()
                .filter(proc -> proc.isUserContentLayer(id))
                .findAny()
                .map(UserLayerService::getBaselayerId)
                .orElse(-1);
        if(userLayerId != -1) {
            return userLayerId;
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

        // Find out if we need custom filter or postProcessing for features
        Optional<UserLayerService> processor =
                userContentProcessors.stream()
                .filter(proc -> proc.isUserContentLayer(id))
                .findAny();
        Filter filter = processor.map(proc-> proc.getWFSFilter(id, uuid, bbox)).orElse(null);

        SimpleFeatureCollection sfc = wfsClient.tryGetFeatures(endPoint, version, user, pass, typeName, bbox, crs, maxFeatures, filter);
        if(processor.isPresent()) {
            try {
                sfc = processor.get().postProcess(sfc);
            } catch (Exception e) {
                throw new ServiceRuntimeException("Failed to post-process user layer", e);
            }
        }
        return sfc;
    }

}
