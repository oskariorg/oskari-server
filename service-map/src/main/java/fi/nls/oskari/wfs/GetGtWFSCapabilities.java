package fi.nls.oskari.wfs;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.geotools.data.DataStore;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Methods for parsing WFS capabilities data
 * Prototype
 */
public class GetGtWFSCapabilities {

    private static final Logger log = LogFactory.getLogger(GetGtWFSCapabilities.class);

    private final static String KEY_LAYERS = "layers";

    private final static LayerJSONFormatterWFS FORMATTER = new LayerJSONFormatterWFS();

    /**
     * Get all WFS layers (featuretypes) data in JSON
     * * @param rurl WFS service url
     *
     * @return
     * @throws fi.nls.oskari.service.ServiceException
     *
     */
    public static JSONObject getWFSCapabilities(final String rurl, final String version) throws ServiceException {
        try {

            Map connectionParameters = new HashMap();
            connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL" , getUrl(rurl, version));
            connectionParameters.put("WFSDataStoreFactory:TIMEOUT" , 30000);

            //  connection
            DataStore data = DataStoreFinder.getDataStore(connectionParameters);

            WFSDataStore wfsds = null;
            if (data instanceof WFSDataStore) wfsds = (WFSDataStore) data;

            return parseLayer(wfsds, rurl);
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wfs capabilities response from url." , ex);
        }
    }

    /**
     * Parse layer (group- or wmslayer)
     *
     * @param data geotools wfs DataStore
     * @throws fi.nls.oskari.service.ServiceException
     *
     */
    public static JSONObject parseLayer(WFSDataStore data, String rurl) throws ServiceException {
        if (data == null) {
            return null;
        }
        try {
            // Feature types
            String typeNames[] = data.getTypeNames();

            // json layers array
            final JSONObject wfsLayers = new JSONObject();

            if (typeNames.length > 0) {
                // Add group of layers

                JSONArray layers = new JSONArray();
                wfsLayers.put(KEY_LAYERS, layers);

                // Loop feature types
                for (String typeName : typeNames) {
                    layers.put(layerToOskariLayerJson(data, typeName, rurl));
                }
            }

            return wfsLayers;

        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wms capabilities layer" , ex);
        }
    }

    /**
     * WMS layer data to json
     *
     * @param data     geotools wfs DataStore
     * @param typeName
     * @return WFSlayers
     * @throws fi.nls.oskari.service.ServiceException
     *
     */
    public static JSONObject layerToOskariLayerJson(WFSDataStore data, String typeName, String rurl) throws ServiceException {

        final OskariLayer oskariLayer = new OskariLayer();
        oskariLayer.setType(OskariLayer.TYPE_WFS);
        oskariLayer.setUrl(rurl);
        // THIS IS ON PURPOSE: min -> max, max -> min
        oskariLayer.setMaxScale(1d);
        oskariLayer.setMinScale(1500000d);
        oskariLayer.setName(typeName);
        String title = "";

        try {
            SimpleFeatureType schema = data.getSchema(typeName);

            // Source
            FeatureSource<SimpleFeatureType, SimpleFeature> source  = data.getFeatureSource(typeName);
            title = source.getInfo().getTitle();

            // Geometry property
            String geomName = schema.getGeometryDescriptor().getName().getLocalPart();

            // setup UI names for all supported languages
            final String[] languages = PropertyUtil.getSupportedLanguages();
            for (String lang : languages) {
                oskariLayer.setName(lang, title);
            }

             // JSON formatter will parse uuid from url
            // Metadataurl is in featureType, but no method to get it

     /*       final List<MetadataURL> meta = capabilitiesLayer.getMetadataURL();
            if (meta != null) {
                if (meta.size() > 0)
                {
                    oskariLayer.setMetadataId(meta.get(0).getUrl().toString());
                }
            }  */

        } catch (Exception ex) {
            log.warn("Couldn't get wfs feature source data" , ex);
            return new JSONObject();
        }

        try {

            JSONObject json = FORMATTER.getJSON(oskariLayer, PropertyUtil.getDefaultLanguage(), false);
            // add/modify admin specific fields
            OskariLayerWorker.modifyCommonFieldsForEditing(json, oskariLayer);
            // for admin ui only
            JSONHelper.putValue(json, "title" , title);

            // NOTE! Important to remove id since this is at template
            json.remove("id");
            // ---------------
            return json;
        } catch (Exception ex) {
            log.warn("Couldn't parse wfslayer to json" , ex);
            return new JSONObject();
        }
    }

    /**
     * Finalise WMS service url for GetCapabilities request
     *
     * @param urlin
     * @return
     */
    public static String getUrl(String urlin, String version) {

        if (urlin.isEmpty())
            return "";
        String url = urlin;
        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "service=WFS";
            if (url.toLowerCase().indexOf("getcapabilities") == -1)
                url = url + "&request=GetCapabilities";
        } else {
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "&service=WFS";
            if (url.toLowerCase().indexOf("getcapabilities") == -1)
                url = url + "&request=GetCapabilities";

        }
        if (url.toLowerCase().indexOf("version") == -1)
            url = url + "&version=" + version;

        return url;
    }

}
