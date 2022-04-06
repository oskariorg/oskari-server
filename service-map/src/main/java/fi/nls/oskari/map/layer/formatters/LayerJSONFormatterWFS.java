package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.*;
import org.eclipse.emf.ecore.EObject;
import org.geotools.data.ResourceInfo;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.internal.WFSGetCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.oskari.capabilities.ogc.api.features.OGCAPIFeaturesService;

import java.util.Collections;
import java.util.Set;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;
/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterWFS extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWFS.class);

    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, true);

        return layerJson;
    }
    public static JSONObject createCapabilitiesJSON(final WFSGetCapabilities capa, SimpleFeatureSource source, Set<String> systemCRSs) throws ServiceException {

        try {
            JSONObject json;
            EObject parsedCapa = capa.getParsedCapabilities();
            if (parsedCapa instanceof net.opengis.wfs20.WFSCapabilitiesType) {
                json = WFSCapabilitiesParser200.parse(
                        (net.opengis.wfs20.WFSCapabilitiesType) parsedCapa);
            } else if (parsedCapa instanceof net.opengis.wfs.WFSCapabilitiesType) {
                json = WFSCapabilitiesParser110.parse(
                        (net.opengis.wfs.WFSCapabilitiesType) parsedCapa);
            } else {
                throw new ServiceException("Invalid WFSCapabilitiesType");
            }
            // parse for version 1.x
            // TODO: 2.0.0 or newer doesn't work with this so content-editor etc will not work with those
            // Schema is used only to parse geometry property name
            // skip if failed to get schema or can't find default geometry property
            try {
                SimpleFeatureType sft = source.getSchema();
                JSONHelper.putValue(json, CapabilitiesConstants.KEY_NAMESPACE_URL, sft.getName().getNamespaceURI());
                GeometryDescriptor geom = sft.getGeometryDescriptor();
                if (geom != null) {
                    JSONHelper.putValue(json, CapabilitiesConstants.KEY_GEOM_NAME, geom.getLocalName());
                } // TODO: else sft.getTypes().filter(known geom types)
            } catch (Exception e) {
                log.info("Unable to parse namespace url or geometry field name from schema:", e.getMessage());
            }

            ResourceInfo info = source.getInfo();
            // TODO is there more than default crs
            String crs = CRS.lookupIdentifier(info.getCRS(), true);
            if (crs != null) {
                Set<String> crss = getCRSsToStore(systemCRSs, Collections.singleton(crs));
                JSONHelper.put(json, KEY_SRS, new JSONArray(crss));
            }
            ReferencedEnvelope bbox = info.getBounds();
            if (bbox != null) {
                bbox = bbox.transform(WKTHelper.CRS_EPSG_4326, true);
                boolean coversWholeWorld = bbox.getMinX() <= -180 && bbox.getMinY() <= -90 && bbox.getMaxX() >= 180 && bbox.getMaxY() >= 90;
                if (!coversWholeWorld) {
                    // no need to attach coverage if it covers the whole world as it's not useful info
                    String wkt = WKTHelper.getBBOX(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
                    JSONHelper.putValue(json,KEY_LAYER_COVERAGE, wkt);
                }
            }
            Set<String> keywords = info.getKeywords();
            JSONHelper.putValue(json, KEY_KEYWORDS, new JSONArray(keywords));
            return json;
        } catch (Exception e) {
            throw new ServiceException( "Failed to create capabilities json: " + e.getMessage());

        }
    }
    public static JSONObject createCapabilitiesJSON (OGCAPIFeaturesService service, String collectionId, Set<String> systemCRSs) {
        JSONObject capabilities = new JSONObject(); // override
        Set<String> crsUri  = service.getSupportedCrsURIs(collectionId);
        JSONHelper.put(capabilities, KEY_CRS_URI, new JSONArray(crsUri));
        Set<String> capabilitiesCRSs = service.getSupportedEpsgCodes(collectionId);
        Set<String> crss = getCRSsToStore(systemCRSs, capabilitiesCRSs);
        JSONHelper.putValue(capabilities, KEY_SRS, new JSONArray(crss));
        Set<String> formats = service.getSupportedFormats(collectionId);
        JSONHelper.put(capabilities, KEY_FEATURE_OUTPUT_FORMATS, new JSONArray(formats));
        return capabilities;
    }

}
