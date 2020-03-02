package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
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
import org.oskari.service.wfs3.WFS3Service;

import java.util.Collections;
import java.util.List;
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

    private static final String KEY_WPS_PARAMS = "wps_params";
    private static final String KEY_WMS_LAYER_ID = "WMSLayerId";

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWFS.class);

    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        JSONHelper.putValue(layerJson, KEY_STYLES, getStyles(layer.getOptions()));
        // Use maplayer setup
        if(layer.getStyle() == null || layer.getStyle().isEmpty() ){
            JSONHelper.putValue(layerJson, KEY_STYLE, "default");
        }
        else {
            JSONHelper.putValue(layerJson, KEY_STYLE, layer.getStyle());
        }
        JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, true);
        WFSLayerAttributes attr = new WFSLayerAttributes(layer.getAttributes());
        JSONHelper.putValue(layerJson, KEY_WPS_PARAMS, getWpsParams(attr.getWpsParams()) );

        return layerJson;
    }

    /**
     * Constructs a style json
     *
     * @param  options wfs layer configuration
     */
    private JSONArray getStyles(JSONObject options) {

        JSONArray arr = new JSONArray();
        // TODO: parse styles from options
        return arr;
    }

    /**
     * Constructs wps params json
     *
     * @param  wpsParams wfs layer configuration
     */
    private JSONObject getWpsParams(String wpsParams) {

        JSONObject json = new JSONObject();
        if (wpsParams == null) return json;

        return JSONHelper.createJSONObject(wpsParams);

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
            // Schema is used only to parse geometry property name
            // skip if failed to get schema or can't find default geometry property
            try {
                SimpleFeatureType sft = source.getSchema();
                GeometryDescriptor geom = sft.getGeometryDescriptor();
                if (geom != null) {
                    JSONHelper.putValue(json, CapabilitiesConstants.KEY_GEOM_NAME, geom.getLocalName());
                } // TODO: else sft.getTypes().filter(known geom types)
            } catch (Exception e) {}

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
                    // no need to attach coverage as it covers the whole world
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
    public static JSONObject createCapabilitiesJSON (WFS3Service service, String collectionId, Set<String> systemCRSs) {
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
