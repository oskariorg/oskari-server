package fi.nls.oskari.csw.helper;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.oskari.capabilities.ogc.LayerCapabilitiesOGC;

import fi.nls.oskari.csw.dao.OskariLayerMetadataDao;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.dto.OskariLayerMetadataDto;
import fi.nls.oskari.csw.service.CSWService;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;

public class CSW {

    private static final Logger LOG = LogFactory.getLogger(CSW.class);

    private static final String LAYER_ATTRIBUTE_METADATA_URL = "metadataUrl";

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private CSW() {
    }

    public static RefreshResult refreshLayerMetadata(OskariLayerMetadataDao metadataDato, OskariLayer layer) {
        String baseURL = getMetadataServiceBaseURL(layer.getAttributes());
        String metadataid = getMetadataIdForLayer(layer);
        String lang = PropertyUtil.getDefaultLanguage();

        if (metadataid == null || metadataid.isBlank()) {
            return RefreshResult.SKIPPED;
        }

        try {
            CSWIsoRecord rec = getCSWRecord(baseURL, metadataid, lang);
            if (rec == null) {
                return RefreshResult.RECORD_NOT_FOUND;
            }

            String json = rec.toJSON().toString();
            String wkt = getCoverageWKT(rec);
            if (wkt == null) {
                return RefreshResult.GEOMETRY_NOT_FOUND;
            }

            OskariLayerMetadataDto dto = new OskariLayerMetadataDto();
            dto.metadataId = metadataid;
            dto.json = json;
            dto.wkt = wkt;
            metadataDato.saveMetadata(dto);

            return RefreshResult.OK;
        } catch (Exception e) {
            LOG.warn(e, "CSW metadata handling failed, baseURL", baseURL, "id", metadataid);
            return RefreshResult.FAILED;
        }
    }

    /**
     * Builds the WKT for the overall bounding box covering the envelopes of the first identification.
     * Note! The envelopes are in WGS84 as EX_GeographicBoundingBox is always in decimal degrees.
     *
     * @return WKT for the bbox or null if there are no usable envelopes
     */
    protected static String getCoverageWKT(CSWIsoRecord rec) {
        List<CSWIsoRecord.Envelope> envelopes = rec.getIdentifications().stream()
                .findAny()
                .map(CSWIsoRecord.Identification::getEnvelopes)
                .orElse(Collections.emptyList());

        Envelope coverage = new Envelope();
        for (CSWIsoRecord.Envelope e : envelopes) {
            if (e.getWestBoundLongitude() == null || e.getEastBoundLongitude() == null
                    || e.getSouthBoundLatitude() == null || e.getNorthBoundLatitude() == null) {
                // skip the ones missing any of the bounds
                continue;
            }
            coverage.expandToInclude(e.getWestBoundLongitude(), e.getSouthBoundLatitude());
            coverage.expandToInclude(e.getEastBoundLongitude(), e.getNorthBoundLatitude());
        }
        if (coverage.isNull()) {
            return null;
        }
        return GF.toGeometry(coverage).toText();
    }

    public enum RefreshResult {
        SKIPPED,
        RECORD_NOT_FOUND,
        GEOMETRY_NOT_FOUND,
        OK,
        FAILED
    }

    public static String getMetadataServiceBaseURL(JSONObject layerAttributes) {
        return layerAttributes != null && layerAttributes.has(LAYER_ATTRIBUTE_METADATA_URL)
                ? layerAttributes.get(LAYER_ATTRIBUTE_METADATA_URL).toString()
                : getDefaultServiceBaseURL();
    }

    public static String getDefaultServiceBaseURL() {
        return PropertyUtil.getOptional(CSWService.PROP_SERVICE_URL);
    }

    public static String getMetadataIdForLayer(OskariLayer layer) {
        String uuid = layer.getMetadataId();
        if (uuid != null && !uuid.trim().isEmpty()) {
            // override metadataid
            return uuid.trim();
        }
        // uuid from capabilities
        return layer.getCapabilities().optString(LayerCapabilitiesOGC.METADATA_UUID, null);
    }

    public static CSWIsoRecord getCSWRecord(String serviceUrl, String metadataid, String lang) throws ServiceException {
        CSWService service = new CSWService(serviceUrl);
        try {
            return service.getRecordById(metadataid, lang);
        } catch (Exception e) {
            throw new ServiceException("Failed to query service: " + e.getMessage());
        }
    }

}
