package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
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
    private static WFSLayerConfigurationService wfsService = new WFSLayerConfigurationServiceIbatisImpl();


    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        final WFSLayerConfiguration wfsConf = wfsService.findConfiguration(layer.getId());
        JSONHelper.putValue(layerJson, KEY_STYLES, getStyles(wfsConf));
        // Use maplayer setup
        if(layer.getStyle() == null || layer.getStyle().isEmpty() ){
            JSONHelper.putValue(layerJson, KEY_STYLE, "default");
        }
        else {
            JSONHelper.putValue(layerJson, KEY_STYLE, layer.getStyle());
        }
        JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, true);
        JSONHelper.putValue(layerJson, KEY_WPS_PARAMS, getWpsParams(wfsConf) );
        if(wfsConf != null){
            JSONHelper.putValue(layerJson, KEY_WMS_LAYER_ID, wfsConf.getWMSLayerId() );
        }

        return layerJson;
    }

    /**
     * Constructs a style json
     *
     * @param  wfsConf wfs layer configuration
     */
    private JSONArray getStyles(WFSLayerConfiguration wfsConf) {

        JSONArray arr = new JSONArray();
        if (wfsConf == null) return arr;

        final List<WFSSLDStyle> styleList = wfsConf.getSLDStyles();
        if (styleList == null) return arr;

        try {
            for (WFSSLDStyle style : styleList) {
                JSONObject obj = createStylesJSON(style.getName(), style.getName(), style.getName());
                if (obj.length() > 0) {
                    arr.put(obj);
                }
            }
        } catch (Exception e) {
          log.warn("Failed to query wfs styles via SQL client");
        }
        return arr;
    }

    /**
     * Constructs wps params json
     *
     * @param  wfsConf wfs layer configuration
     */
    private JSONObject getWpsParams(WFSLayerConfiguration wfsConf) {

        JSONObject json = new JSONObject();
        if (wfsConf == null) return json;

        return JSONHelper.createJSONObject(wfsConf.getWps_params());

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
                json = new JSONObject();
            }
            SimpleFeatureType sft = source.getSchema();
            GeometryDescriptor geom = sft.getGeometryDescriptor();
            if (geom != null) {
                JSONHelper.putValue(json, CapabilitiesConstants.KEY_GEOM_NAME, geom.getLocalName());
            } // TODO: else sft.getTypes().filter(known geom types)

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
                String wkt = WKTHelper.getBBOX(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
                JSONHelper.putValue(json,KEY_LAYER_COVERAGE, wkt);
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
