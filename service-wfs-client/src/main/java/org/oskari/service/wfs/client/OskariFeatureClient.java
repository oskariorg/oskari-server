package org.oskari.service.wfs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.wfs3.CoordinateTransformer;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_FEATURE_OUTPUT_FORMATS;

public class OskariFeatureClient {

    protected static final String PROPERTY_NATIVE_SRS = "oskari.native.srs";
    protected static final String PROPERTY_FORCE_GML = "forceGML";
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
            CoordinateReferenceSystem targetCRS, Optional<UserLayerService> processor) throws ServiceRuntimeException {
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
            Optional<UserLayerService> processor) throws ServiceRuntimeException {
        String endPoint = layer.getUrl();
        String version = layer.getVersion();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();

        List <String> formats =  new ArrayList<>();
        JSONObject attributes = layer.getAttributes();
        JSONObject capa = layer.getCapabilities();
        if (attributes.has(PROPERTY_FORCE_GML) && attributes.optBoolean(PROPERTY_FORCE_GML, false)) {
            formats.add(getForcedGMLFormat(version));
        } else if (capa.has(KEY_FEATURE_OUTPUT_FORMATS)) {
            JSONArray arr = JSONHelper.getEmptyIfNull(
                    JSONHelper.getJSONArray(capa, KEY_FEATURE_OUTPUT_FORMATS));
            formats = JSONHelper.getArrayAsList(arr);
        }
        // TODO: Figure out the maxFeatures from the layer
        int maxFeatures = 10000;

        Filter filter = processor.map(proc -> proc.getWFSFilter(id, bbox)).orElse(null);

        SimpleFeatureCollection sfc = wfsClient.getFeatures(endPoint, version, user, pass, typeName, bbox, crs, maxFeatures, filter, formats);

        if (processor.isPresent()) {
            try {
                sfc = processor.get().postProcess(sfc);
            } catch (Exception e) {
                throw new ServiceRuntimeException("Failed to post-process user layer", e);
            }
        }

        return sfc;
    }
    private static String getForcedGMLFormat (String version) {
        if ("2.0.0".equals(version)) return "application/gml+xml; version=3.2";
        return "text/xml; subtype=gml/3.1.1";
    }
}
