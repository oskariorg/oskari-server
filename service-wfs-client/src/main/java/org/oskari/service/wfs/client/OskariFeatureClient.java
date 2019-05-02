package org.oskari.service.wfs.client;

import java.util.Objects;
import java.util.Optional;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.wfs3.CoordinateTransformer;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;

public class OskariFeatureClient {

    protected static final String PROPERTY_NATIVE_SRS = "oskari.native.srs";
    protected static final String ERR_REPOJECTION_FAIL = "Reprojection failed";
    protected static final String ERR_NATIVE_SRS_DECODE_FAIL = "Failed to decode Native CRS";

    private OskariWFSClient wfsClient;
    private CoordinateReferenceSystem nativeCRS;

    public OskariFeatureClient(OskariWFSClient wfsClient) {
        this.wfsClient = Objects.requireNonNull(wfsClient);
    }

    protected CoordinateReferenceSystem getNativeCRS() {
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

    public SimpleFeatureCollection getFeatures(String id, String uuid, OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem nativeCRS, CoordinateReferenceSystem targetCRS,
            Optional<UserLayerService> processor) throws ServiceException {
        boolean needsTransform = !CRS.equalsIgnoreMetadata(nativeCRS, targetCRS);

        // Request features in nativeCRS (of the installation)
        // Most likely supported by all WFS layers
        ReferencedEnvelope requestBbox = bbox;
        if (needsTransform) {
            try {
                requestBbox = bbox.transform(nativeCRS, true);
            } catch (Exception e) {
                throw new ServiceException(ERR_REPOJECTION_FAIL, e);
            }
        }

        SimpleFeatureCollection features = getFeatures(id, uuid, layer, requestBbox, nativeCRS, processor);
        if (!needsTransform) {
            return features;
        }

        // Transform features to targetCRS
        try {
            CoordinateTransformer transformer = new CoordinateTransformer(nativeCRS, targetCRS);
            return transformer.transform(features);
        } catch (Exception e) {
            throw new ServiceException(ERR_REPOJECTION_FAIL, e);
        }
    }

    public SimpleFeatureCollection getFeatures(String id, String uuid, OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            Optional<UserLayerService> processor) throws ServiceRuntimeException {
        String endPoint = layer.getUrl();
        String version = layer.getVersion();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();
        // TODO: Figure out the maxFeatures from the layer
        int maxFeatures = 10000;

        Filter filter = processor.map(proc -> proc.getWFSFilter(id, bbox)).orElse(null);

        SimpleFeatureCollection sfc = wfsClient.getFeatures(endPoint, version, user, pass, typeName, bbox, crs, maxFeatures, filter);

        if (processor.isPresent()) {
            try {
                sfc = processor.get().postProcess(sfc);
            } catch (Exception e) {
                throw new ServiceRuntimeException("Failed to post-process user layer", e);
            }
        }

        return sfc;
    }

    public boolean isWithin(CoordinateReferenceSystem targetCRS, Envelope bbox) {
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

}
