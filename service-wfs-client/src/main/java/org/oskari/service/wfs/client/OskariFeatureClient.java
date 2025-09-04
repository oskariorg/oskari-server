package org.oskari.service.wfs.client;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.wfs3.CoordinateTransformer;

import java.util.Objects;
import java.util.Optional;

public class OskariFeatureClient {
    public static final Logger LOG = LogFactory.getLogger(OskariFeatureClient.class);

    protected static final String PROPERTY_NATIVE_SRS = "oskari.native.srs";
    protected static final String ERR_REPOJECTION_FAIL = "Reprojection failed";
    protected static final String ERR_NATIVE_SRS_DECODE_FAIL = "Failed to decode Native CRS";

    private OskariWFSClient wfsClient;
    private CoordinateReferenceSystem nativeCRS;

    public OskariFeatureClient(OskariWFSClient wfsClient) {
        this.wfsClient = Objects.requireNonNull(wfsClient);
    }

    private CoordinateReferenceSystem getNativeCRS() {
        if (nativeCRS == null) {
            try {
                String nativeSrs = PropertyUtil.get(PROPERTY_NATIVE_SRS, "EPSG:4326");
                nativeCRS = CRS.decode(nativeSrs, true);
            } catch (Exception e) {
                throw new ServiceRuntimeException(ERR_NATIVE_SRS_DECODE_FAIL, e);
            }
        }
        return nativeCRS;
    }

    public SimpleFeatureCollection getFeatures(String id, OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem targetCRS, Optional<UserLayerService> processor) {
        CoordinateReferenceSystem nativeCRS = getNativeCRS();
        boolean needsTransform = !CRS.equalsIgnoreMetadata(nativeCRS, targetCRS);

        // Request features in nativeCRS (of the installation)
        // Most likely supported by all WFS layers
        ReferencedEnvelope requestBbox = bbox;
        if (needsTransform) {
            try {
                requestBbox = bbox.transform(nativeCRS, true);
            } catch (Exception e) {
                throw new ServiceRuntimeException(ERR_REPOJECTION_FAIL, e);
            }
        }

        SimpleFeatureCollection features = getFeaturesNoTransform(id, layer, requestBbox, nativeCRS, processor);

        if (!needsTransform) {
            return features;
        }

        // Transform features to targetCRS
        try {
            CoordinateTransformer transformer = new CoordinateTransformer(nativeCRS, targetCRS);
            return transformer.transform(features);
        } catch (Exception e) {
            throw new ServiceRuntimeException(ERR_REPOJECTION_FAIL, e);
        }
    }

    private SimpleFeatureCollection getFeaturesNoTransform(String id, OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            Optional<UserLayerService> processor) {
        if (processor.isPresent()) {
            try {
                return processor.get().getFeatures(id, bbox);
            } catch (ServiceException e) {
                throw new ServiceRuntimeException("Failed to get features for user layer", e);
            }
        }
    
        Filter filter = OskariWFSClient.getWFSFilter(id, layer, bbox);
        return wfsClient.getFeatures(layer, bbox, crs, filter);
    }
}
