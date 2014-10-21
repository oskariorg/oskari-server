package fi.nls.oskari.wfs;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
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
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;

import java.io.IOException;
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
    private final static String DEFAULT_VERSION = "1.1.0";
    private final static LayerJSONFormatterWFS FORMATTER = new LayerJSONFormatterWFS();

    /**
     * Get all WFS layers (featuretypes) data in JSON
     * * @param rurl WFS service url
     *
     * @return json of wfslayer json array
     * @throws fi.nls.oskari.service.ServiceException
     *
     */
    public static JSONObject getWFSCapabilities(final String rurl, final String version, final String user, final String pw) throws ServiceException {
        try {
            String wfs_version = version;
            if(version.isEmpty()) wfs_version = DEFAULT_VERSION;
            Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(rurl, wfs_version, user, pw);
            if (capa == null || !capa.containsKey("WFSDataStore"))  throw new ServiceException("Couldn't read/get wfs capabilities response from url." );
            try {

                WFSDataStore wfsds = (WFSDataStore) capa.get("WFSDataStore");
                return parseLayer(wfsds, rurl, user, pw);

            } catch (Exception ex) {
                throw new ServiceException("Couldn't read/get wfs capabilities response from url." , ex);
            }

        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wfs capabilities response from url." , ex);
        }
    }

    /**
     * Get all WFS layers (featuretypes) data in JSON
     * @param rurl WFS service url
     * @param version WFS service version
     * @return json of wfslayer json array

     *
     */
    public static Map<String, Object> getGtDataStoreCapabilities(final String rurl, final String version, String user, String pw) {

        Map<String, Object> capabilities = new HashMap<String, Object>();
        try {

            Map connectionParameters = new HashMap();
            connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL" , getUrl(rurl, version));
            connectionParameters.put("WFSDataStoreFactory:TIMEOUT" , 30000);
            if(user != null && !user.isEmpty()) {
                connectionParameters.put("WFSDataStoreFactory:USERNAME" , user);
                connectionParameters.put("WFSDataStoreFactory:PASSWORD" , pw);
            }

            //  connection
            DataStore data = DataStoreFinder.getDataStore(connectionParameters);

            WFSDataStore wfsds = null;
            if (data instanceof WFSDataStore) wfsds = (WFSDataStore) data;
            if(wfsds != null){
                capabilities.put("status", "OK");
                capabilities.put("WFSDataStore", wfsds);
            }
            else {
                capabilities.put("status", "FAILED");
                capabilities.put("exception", "Not instance of WFSDataStore");
            }
        } catch (Exception ex) {
            capabilities.put("status", "FAILED");
            capabilities.put("exception", ex.getMessage());
        }
        return capabilities;
    }


    /**
     * Parse layer (group- or wmslayer)
     *
     * @param data geotools wfs DataStore
     * @throws fi.nls.oskari.service.ServiceException
     *
     */
    public static JSONObject parseLayer(WFSDataStore data, String rurl, String user, String pw) throws ServiceException {
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
                    layers.put(layerToOskariLayerJson(data, typeName, rurl, user, pw));
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
    public static JSONObject layerToOskariLayerJson(WFSDataStore data, String typeName, String rurl, String user, String pw) throws ServiceException {

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
            //FIXME  merge oskarilayer and wfsLayer
            WFSLayerConfiguration lc = GetGtWFSCapabilities.layerToWfsLayerConfiguration(data, typeName, rurl, user, pw);

            JSONHelper.putValue(json.getJSONObject("admin"), "passthrough" , JSONHelper.createJSONObject(lc.getAsJSON()));

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
     * WMS layer data to json
     *
     * @param data     geotools wfs DataStore
     * @param typeName
     * @return WFSLayerConfiguration  wfs feature type properties for wfs service and oskari rendering
     * @throws fi.nls.oskari.service.ServiceException
     *
     */
    public static WFSLayerConfiguration  layerToWfsLayerConfiguration(WFSDataStore data, String typeName, String rurl, String user, String pw) {

        final WFSLayerConfiguration lc = new WFSLayerConfiguration();
        lc.setDefaults();


        lc.setURL(rurl);
        lc.setUsername(user);
        lc.setPassword(pw);

        lc.setLayerName(typeName);


        try {
            SimpleFeatureType schema = data.getSchema(typeName);

            String [] nameParts = schema.getName().getLocalPart().split(schema.getName().getSeparator());
            String xmlns = "";
            String name = nameParts[0];
            if(nameParts.length > 1){
                xmlns = nameParts[0];
                name = nameParts[1];
            }

            lc.setLayerId("layer_" + name);

            // Geometry property
            String geomName = schema.getGeometryDescriptor().getName().getLocalPart();
            int isrs = CRS.lookupEpsgCode(schema.getCoordinateReferenceSystem(), true);


            lc.setGMLGeometryProperty(geomName);
            lc.setSRSName("EPSG:"+Integer.toString(isrs));

            //lc.setGMLVersion();
            lc.setWFSVersion(data.getInfo().getVersion());
            //lc.setMaxFeatures(data.getMaxFeatures());
            lc.setFeatureNamespace(xmlns);
            lc.setFeatureNamespaceURI(schema.getName().getNamespaceURI());

            lc.setFeatureElement(name);

            return lc;
        } catch (Exception ex) {
            log.warn("Couldn't get wfs feature source data" , ex);
            return null;
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
