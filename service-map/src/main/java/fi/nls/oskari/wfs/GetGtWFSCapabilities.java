package fi.nls.oskari.wfs;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFS2FeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wfs.util.WFSParserConfigs;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.internal.WFSGetCapabilities;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_FEATURE_OUTPUT_FORMATS;


/**
 * Methods for parsing WFS capabilities data
 * Prototype
 */
public class GetGtWFSCapabilities {

    private static final Logger log = LogFactory.getLogger(GetGtWFSCapabilities.class);

    private static final String KEY_LAYERS = "layers";
    private static final String KEY_LAYERS_WITH_ERRORS = "layersWithErrors";
    private static final String KEY_LAYERS_WITH_REMARKS = "layersWithRemarks";
    private static final String KEY_ALLOWED_FORMATS = "allowedFormats";
    private static final String KEY_WFS1_DATA = "WFSDataStore";
    private static final String KEY_WFS2_DATA = "FeatureTypeList";
    private static final String KEY_JSON_CAPA_FORMATS = "formats";
    private static final String KEY_JSON_CAPA_AVAILABLE = "available";
    private static final String DEFAULT_VERSION = "1.1.0";
    private static final String WFS2_0_0_VERSION = "2.0.0";
    private static final String DEFAULT_GEOMETRY_NAME = "geometry";
    private static final LayerJSONFormatterWFS FORMATTER = new LayerJSONFormatterWFS();
    private static final List<String> GEOMTYPES = new ArrayList<>(Arrays.asList("gml:PolygonPropertyType", "gml:SurfacePropertyType",
            "gml:PolyhedralSurfacePropertyType", "gml:TriangulatedSurfacePropertyType", "gml:TinPropertyType",
            "gml:OrientableSurfacePropertyType", "gml:CompositeSurfacePropertyType", "gml:LineStringPropertyType",
            "gml:CurvePropertyType", "gml:CompositeCurvePropertyType", "gml:OrientableCurvePropertyType", "gml:MultiCurvePropertyType",
            "gml:PointPropertyType",
            "gml:MultiSurfacePropertyType",
            "gml:MultiPointPropertyType"));

    //TODO: use resource file for config predefined uris
    private static final Map<String,String>  uris = new HashMap<String,String>();


    /**
     * Get all WFS layers (featuretypes) data in JSON
     * * @param rurl WFS service url
     *
     * @return json of wfslayer json array
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static JSONObject getWFSCapabilities(final String rurl, final String version, final String user,
                                                final String pw, final String currentCrs)
            throws ServiceException {
        String wfs_version = version;
        if (version.isEmpty()) {
            wfs_version = DEFAULT_VERSION;
        }
        // Only default_version and WFS2_0_0_VERSION  are supported
        if (!version.equals(WFS2_0_0_VERSION)) {
            wfs_version = DEFAULT_VERSION;
        }
        Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(rurl, wfs_version, user, pw, currentCrs);

        return parseLayer(capa, wfs_version, rurl, user, pw);
    }
    public static JSONObject getLayerCapabilities (OskariLayer ml, Set<String> systemCRSs) throws ServiceException {
        Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(
                ml.getUrl(), ml.getVersion(), ml.getUsername(), ml.getPassword(), ml.getSrs_name());
        return getLayerCapabilities (capa, ml.getName(), systemCRSs);
    }
    public static JSONObject getLayerCapabilities (Map<String, Object> capa, String layerName, Set<String> systemCRSs) {
        JSONObject capaJSON = new JSONObject(); // override
        Set<String> capaCRSs = GetGtWFSCapabilities.parseProjections(capa, layerName);
        Set<String> crss = FORMATTER.getCRSsToStore(systemCRSs, capaCRSs);
        JSONHelper.put(capaJSON, "srs", new JSONArray(crss));
        if (capa.containsKey(KEY_ALLOWED_FORMATS)) {
            JSONHelper.putValue(capaJSON, KEY_FEATURE_OUTPUT_FORMATS, capa.get(KEY_ALLOWED_FORMATS));
        }
        return capaJSON;
    }
    /**
     * Get all WFS layers (featuretypes) data in JSON
     *
     * @param rurl    WFS service url
     * @param version WFS service version
     * @return json of wfslayer json array
     */
    public static Map<String, Object> getGtDataStoreCapabilities(final String rurl, final String version, String user,
                                                                 String pw, String currentCrs)
                                                                    throws ServiceException {
        if (version.equals(WFS2_0_0_VERSION)) {
            return getGtDataStoreCapabilities_2_x(rurl, version, user, pw, currentCrs);
        } else {
            return getGtDataStoreCapabilities_1_x(rurl, version, user, pw);
        }
    }

    /**
     * Get WFS geotools datastore for WFS versions 1.x
     *
     * @param rurl    WFS service url
     * @param version WFS service version
     * @return HashMap / WFSDataStore of wfs service
     */
    public static Map<String, Object> getGtDataStoreCapabilities_1_x(final String rurl, final String version,
                                                                     String user, String pw)
                                                                        throws ServiceException {

        Map<String, Object> capabilities = new HashMap<String, Object>();
        try {

            Map connectionParameters = new HashMap();
            connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getUrl(rurl, version));
            connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 30000);
            if (user != null && !user.isEmpty()) {
                connectionParameters.put("WFSDataStoreFactory:USERNAME", user);
                connectionParameters.put("WFSDataStoreFactory:PASSWORD", pw);
            }

            //  connection
            DataStore data = DataStoreFinder.getDataStore(connectionParameters);

            WFSDataStore wfsds = null;
            List<String> formats = new ArrayList<>();
            if (data instanceof WFSDataStore) {
                wfsds = (WFSDataStore) data;
                WFSGetCapabilities capa = wfsds.getWfsClient().getCapabilities();
                formats = parseGetFeatureFormats(capa.getRawDocument());
            }
            if (wfsds != null) {
                capabilities.put("status", "OK");
                capabilities.put(KEY_WFS1_DATA, wfsds);
                capabilities.put(KEY_ALLOWED_FORMATS, formats);
            } else {
                throw new ServiceException("Not instance of WFSDataStore - url: " + rurl);
            }
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wfs capabilities response from url: " + rurl + " Message: " + ex.getMessage());
        }
        return capabilities;
    }

    /**
     * Get WFS GetCapabilities data for WFS versions 2.x
     * There is no support for wfs2 WFSDataStore in geotools
     * Use gt-wfs-ng/WFSClient in the future for to parse Capabilities
     *
     * @param rurl    WFS service url
     * @param version WFS service version
     * @return HashMap / HashMap of wfs service featuretypes
     */
    public static Map<String, Object> getGtDataStoreCapabilities_2_x(final String rurl, final String version,
                                                                     String user, String pw, String currentCrs)
                                                                        throws ServiceException {
        WFSParserConfigs parseConfigs = new WFSParserConfigs();
        Map<String, Object> capabilities = new HashMap<String, Object>();
        try {
            // GetCapabilities request
            String data = IOHelper.getURL(getUrl(rurl, version), user, pw);
            final DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
            // dbf.setNamespaceAware(true);  //default false
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document doc = builder.parse(new InputSource(
                    new ByteArrayInputStream(data.getBytes("UTF-8"))));
            NodeList featypes = doc.getDocumentElement().getElementsByTagName("wfs:FeatureType");
            if (featypes.getLength() == 0) {
                featypes = doc.getDocumentElement().getElementsByTagName("FeatureType");
            }
            if (featypes.getLength() == 0) {
                featypes = doc.getDocumentElement().getElementsByTagNameNS(null, "FeatureType");
            }
            //Loop featypes
            Map<String, ArrayList<WFS2FeatureType>> featuretypes = new HashMap<String, ArrayList<WFS2FeatureType>>();
            JSONArray errorTypes = new JSONArray();
            for (int i = 0; i < featypes.getLength(); i++) {
                String nameval = null;
                String titleval = null;
                String srsval = null;
                String name = "Name";
                String title = "Title";
                String srs = "DefaultCRS";
                String OtherSrs = "OtherCRS";
                nameval = scanChildNode(featypes.item(i).getChildNodes(), name, nameval);
                titleval = scanChildNode(featypes.item(i).getChildNodes(), title, titleval);
                srsval = scanChildNode(featypes.item(i).getChildNodes(), srs, srsval);
                String[] otherSrsval = scanChildNodes(featypes.item(i).getChildNodes(), OtherSrs);

                if (nameval != null) {
                    if (titleval == null) {
                        titleval = nameval;
                    }
                    // Loop configs
                    JSONArray feaconffa = parseConfigs.getFeatureTypeConfig(nameval);
                    int count = 1;
                    if (feaconffa != null) {
                        count = feaconffa.length();
                    }
                    ArrayList<WFS2FeatureType> lft = new ArrayList<WFS2FeatureType>();
                    for (int k = 0; k < count; k++) {
                        JSONObject feaconf = JSONHelper.getJSONObject(feaconffa, k);

                        WFS2FeatureType tmpft = new WFS2FeatureType();
                        tmpft.setName(nameval);
                        tmpft.setTitle(titleval);
                        tmpft.setDefaultSrs(srsval);
                        if (otherSrsval.length > 0) {
                            tmpft.setOtherSrs(otherSrsval);
                        }
                        final String extraAppend = isCurrentCRSinCapabilities(tmpft, currentCrs);
                        // Try to parse describe feature type response of wfs 2.0.0 service at least namespaceUri and geometry property
                        try {
                            parseWfs2xDescribeFeatureType(tmpft, IOHelper.getURL(getDescribeFeatureTypeUrl(rurl, version, nameval), user, pw));
                        } catch (Exception e) {
                            addLayerWithError(errorTypes, nameval, titleval, e.getMessage());
                            continue;
                        }
                        // Append parser type to title
                        parserConfigType2Title(feaconf, tmpft, parseConfigs, extraAppend);
                        lft.add(tmpft);
                        featuretypes.put(nameval, lft);
                    }
                }
            }
            List<String> formats = parseGetFeatureFormats(doc);


            if (featypes.getLength() > 0) {
                capabilities.put("status", "OK");
                capabilities.put(KEY_WFS2_DATA, featuretypes);
                capabilities.put("ErrorTypeList", errorTypes);
                capabilities.put(KEY_ALLOWED_FORMATS, formats);
            } else {
                throw new ServiceException("No featuretypes found - url: " + rurl);
            }
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wfs capabilities response from url: " + rurl + " Message: " + ex.getMessage());
        }
        return capabilities;
    }

    /**
     * Parse layer wfs 1.x
     *
     * @param capa geotools wfs DataStore
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static JSONObject parseLayer(Map<String, Object> capa, String version, String rurl, String user, String pw)
            throws ServiceException {
        if (capa == null) {
            return null;
        }
        JSONObject layer = null;
        if (capa.containsKey(KEY_WFS1_DATA)) {
            layer = parseWfs1xLayer(capa, version, rurl, user, pw);
        } else if (capa.containsKey(KEY_WFS2_DATA)) {
            layer = parseWfs2xLayer(capa, version, rurl, user, pw);
        }
        // add layers that failed in DescripeFeatureType request
        if (layer != null && capa.containsKey("FailedTypeList")) {
            JSONArray json = (JSONArray) capa.get("FailedTypeList");
            JSONArray target = JSONHelper.getJSONArray(layer, KEY_LAYERS_WITH_ERRORS);
            for (int i = 0; i < json.length(); i++) {
                target.put(JSONHelper.getJSONObject(json, i));
            }
        }
        return layer;

    }

    private static List <String> parseGetFeatureFormats (Document doc) {
        Element elem = doc.getDocumentElement();
        // Loop operations metadata
        NodeList nodes  = elem.getElementsByTagName("ows:Operation");
        if (nodes.getLength() == 0) {
            nodes = elem.getElementsByTagName("Operation");
        }
        if (nodes.getLength() == 0) {
            nodes = elem.getElementsByTagNameNS(null, "Operation");
        }
        Node node = getChildNodesByAttributeName (nodes, "GetFeature");

        if (node == null) return new ArrayList<>();
        node = getChildNodesByAttributeName(node.getChildNodes(), "outputFormat");

        if (node == null) return new ArrayList<>();
        // return values from AllowedValues element if given (WFS2)
        Node allowed = getChildNodes(node.getChildNodes(), "AllowedValues", null);
        if (allowed != null) {
            return getChildNodeValues(allowed.getChildNodes(), "Value");
        }
        // return from Parameter outputFormat element (WFS1x)
        return getChildNodeValues(node.getChildNodes(), "Value");
    }

    /**
     * Parse supported crs Projections  wfs 1.1.0 or wfs 2.0.0
     *
     * @param capa  capabilities including WFS datastore or FeatureTypeList
     * @param name
     * @return
     */
    public static Set<String> parseProjections(Map<String, Object> capa, String name)
    {
        if (capa == null) {
            return null;
        }
        if (capa.containsKey(KEY_WFS1_DATA)) {
            WFSDataStore data = (WFSDataStore) capa.get(KEY_WFS1_DATA);
            return parseWfs1xProjections(data, name);
        } else if (capa.containsKey(KEY_WFS2_DATA)) {
            Map<String,  ArrayList<WFS2FeatureType>> data = (Map<String,  ArrayList<WFS2FeatureType>>) capa.get(KEY_WFS2_DATA);
            return parseWfs2xProjections(data, name);
        }
        return null;

    }

    /**
     * Parse layer wfs 2.x
     *
     * @param capa capabilites
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static JSONObject parseWfs2xLayer(Map<String, Object> capa, String version, String rurl,
                                             String user, String pw)
            throws ServiceException {
        Map<String, ArrayList<WFS2FeatureType>> typeNames = (Map<String, ArrayList<WFS2FeatureType>>) capa.get(KEY_WFS2_DATA);
        if (typeNames == null) {
            return null;
        }
        try {
            // Feature types

            // json layers array
            final JSONObject wfsLayers = new JSONObject();


            // Add group of layers

            JSONArray layers = new JSONArray();

            JSONArray layersWithErrors = new JSONArray();
            wfsLayers.put(KEY_LAYERS, layers);
            wfsLayers.put(KEY_LAYERS_WITH_ERRORS, layersWithErrors);

            // Loop feature types - Array is used because of duplicate key valyes
            Iterator it = typeNames.keySet().iterator();
            ArrayList<WFS2FeatureType> feaList = null;

            while (it.hasNext()) {
                String key = it.next().toString();
                feaList = typeNames.get(key);
                if (feaList != null) {
                    for (WFS2FeatureType fea2x: feaList) {
                            String typeName = fea2x.getName();
                            try {
                                // no need to filter with system crss because capabilities are updated on save
                                JSONObject capaJSON = getLayerCapabilities(capa, typeName, null);
                                String title = fea2x.getTitle();
                                JSONObject temp = layerToOskariLayerJson(fea2x, title, capaJSON, version, typeName, rurl, user, pw);
                                if (temp != null) {
                                    layers.put(temp);
                                    // Simple remark check
                                    if(title != null && title.indexOf("*") > -1){
                                        wfsLayers.put(KEY_LAYERS_WITH_REMARKS, "true");
                                    }
                                }
                            } catch (ServiceException se) {
                                addLayerWithError(layersWithErrors, typeName, fea2x.getTitle(), se.getMessage());
                            }
                        }
                }
            }

            return wfsLayers;

        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wfs 2.x capabilities layer", ex);
        }
    }
    private static void addLayerWithError (JSONArray target, String name, String title, String error) {
        JSONObject errorLayer = new JSONObject();
        JSONHelper.putValue(errorLayer, "title", title);
        JSONHelper.putValue(errorLayer, "layerName", name);
        JSONHelper.putValue(errorLayer, "errorMessage", error);
        target.put(errorLayer);
    }
    /**
     * Parse layer wfs 1.x
     *
     * @param capa capabilities including geotools wfs DataStore
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static JSONObject parseWfs1xLayer(Map<String, Object> capa, String version, String rurl, String user, String pw)
            throws ServiceException {
        WFSDataStore data = (WFSDataStore) capa.get(KEY_WFS1_DATA);
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

                JSONArray layersWithErrors = new JSONArray();
                wfsLayers.put(KEY_LAYERS, layers);
                wfsLayers.put(KEY_LAYERS_WITH_ERRORS, layersWithErrors);

                // Loop feature types
                for (String typeName : typeNames) {
                    try {
                        // no need to filter with system crss because capabilities are updated on save
                        JSONObject capaJSON = getLayerCapabilities(capa, typeName, null);
                        SimpleFeatureType sft = getSchema(data, typeName);
                        JSONObject temp;
                        // try own DescribeFeature request and parsing (WFS2FeatureType)
                        if (sft == null) {
                            WFS2FeatureType fea2  = parseWfs1xDescribeFeatureType(IOHelper.getURL(getDescribeFeatureTypeUrl(rurl, version, typeName), user, pw), typeName, data);
                            temp = layerToOskariLayerJson(fea2, fea2.getTitle(), capaJSON, version, typeName, rurl, user, pw);
                        } else {
                            String title = data.getFeatureSource(typeName).getInfo().getTitle();
                            temp = layerToOskariLayerJson(sft, title, capaJSON, version, typeName, rurl, user, pw);
                        }
                        if (temp != null) {
                            layers.put(temp);
                        }
                    } catch (ServiceException se) {
                        addLayerWithError(layersWithErrors, typeName, getFeaturetypeTitle(data, typeName), se.getMessage());
                    }
                }
            }

            return wfsLayers;

        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wfs 1.x capabilities layer", ex);
        }
    }

    /**
     * Parse supported layer projections wfs 1.x
     *
     * @param data geotools wfs DataStore
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static Set<String> parseWfs1xProjections(WFSDataStore data, String typeName) {
        if (data == null || typeName == null) {
            return null;
        }
        Set<String> crss = new HashSet<String>();
        try {
            //There is no way to get Other crs - maybe next gt version will support it - not yet in gt 14.2 or gt 15.0
            //Get only the default crs
            String crs = CRS.lookupIdentifier(data.getFeatureSource(typeName).getInfo().getCRS(), true);
            if(crs != null){
                crss.add(crs);
                return crss;
            }


        } catch (Exception ex) {

        }
        return null;
    }

    /**
     * Parse supported layer projections wfs 2.x
     *
     * @param typeNames  wfs featuretype list
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static Set<String> parseWfs2xProjections(Map<String,  ArrayList<WFS2FeatureType>> typeNames, String typeName) {

        if (typeNames == null || typeName == null) {
            return null;
        }
        Set<String> crss = new HashSet<String>();
        try {
            // Array is used because of duplicate key values  - data config could be not equal to same featuretype
            // Use 1st one in this context
            ArrayList<WFS2FeatureType> arrFeas = typeNames.get(typeName);
            WFS2FeatureType  featureType = arrFeas.get(0);
             crss.add(ProjectionHelper.shortSyntaxEpsg(featureType.getDefaultSrs()));
            if(featureType.getOtherSrs() != null && featureType.getOtherSrs().length > 0 )
            for (String s: featureType.getOtherSrs()) {
                crss.add(ProjectionHelper.shortSyntaxEpsg(s));
            }
            return crss;

        } catch (Exception ex) {
            log.warn("Couldn't get wfs supported feature CRSs - exception: ", ex);
        }
        return null;
    }

    public static String isCurrentCRSinCapabilities(WFS2FeatureType featureType, String currentCrs) {

        if (featureType == null || currentCrs == null  ) {
            return "";
        }
        Set<String> crss = new HashSet<String>();
        try {
            // Array is used because of duplicate key values  - data config could be not equal to same featuretype
            // Use 1st one in this context
            crss.add(ProjectionHelper.shortSyntaxEpsg(featureType.getDefaultSrs()));
            if(featureType.getOtherSrs() != null && featureType.getOtherSrs().length > 0 )
                for (String s: featureType.getOtherSrs()) {
                    crss.add(ProjectionHelper.shortSyntaxEpsg(s));
                }
            if(!crss.contains(currentCrs)) {
                return " *";
            }
            return "";
        } catch (Exception ex) {
            log.warn("Couldn't compare map CRS to features supported CRSs - exception: ", ex);
        }
        return "**";
    }

    private static SimpleFeatureType getSchema(WFSDataStore data, String typeName) {

        try {
            return data.getSchema(typeName);
        } catch (Exception ex) {
            log.warn("Couldn't get wfs feature source data", ex);
        }
        return null;
    }

    /**
     * WMS layer data to json
     *
     * @param capa     geotools wfs DataStore or featuretypelist
     * @param typeName
     * @return WFSlayers
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static JSONObject layerToOskariLayerJson(Object layer, String title, JSONObject capa, String version,
                                                    String typeName, String rurl, String user, String pw)
            throws ServiceException {

        final OskariLayer oskariLayer = new OskariLayer();
        oskariLayer.setType(OskariLayer.TYPE_WFS);
        oskariLayer.setUrl(rurl);
        // THIS IS ON PURPOSE: min -> max, max -> min
        oskariLayer.setMaxScale(1d);
        oskariLayer.setMinScale(1500000d);
        oskariLayer.setName(typeName);
        oskariLayer.setVersion(version);
        oskariLayer.setCapabilities(capa);
        // setup UI names for all supported languages
        title = title == null ? typeName : title;
        final String[] languages = PropertyUtil.getSupportedLanguages();
        for (String lang : languages) {
            oskariLayer.setName(lang, title);
        }

        SimpleFeatureType sft = layer instanceof SimpleFeatureType ? (SimpleFeatureType) layer : null;
        WFS2FeatureType fea2type = layer instanceof WFS2FeatureType ? (WFS2FeatureType) layer : null;

        try {

            JSONObject json = FORMATTER.getJSON(oskariLayer, PropertyUtil.getDefaultLanguage(), false, null);
            // add/modify admin specific fields
            OskariLayerWorker.modifyCommonFieldsForEditing(json, oskariLayer);
            // for admin ui only
            JSONHelper.putValue(json, "title", title);
            //FIXME  merge oskarilayer and wfsLayer
            WFSLayerConfiguration lc = null;
            if (WFS2_0_0_VERSION.equals(version)) {
                if (fea2type != null) {
                    lc = GetGtWFSCapabilities.layerToWfs20LayerConfiguration(fea2type, rurl, user, pw);
                }
            } else if (fea2type != null) {
                // WFS 1.x  Geotools parse FAILED
                lc = GetGtWFSCapabilities.layerToWfs1xLayerConfiguration(fea2type, rurl, user, pw);
            } else if (sft != null) {
                // WFS 1.x  Geotools parse OK
                lc = GetGtWFSCapabilities.layerToWfsLayerConfiguration(sft, typeName, rurl, user, pw);
            }
            if(lc == null) {
                throw new RuntimeException("Couldn't parse wfs capabilities");
            }
            JSONHelper.putValue(json.getJSONObject("admin"), "passthrough", JSONHelper.createJSONObject(lc.getAsJSON()));

            // NOTE! Important to remove id since this is at template
            json.remove("id");
            // ---------------
            return json;
        } catch (Exception ex) {
            log.warn(ex, "Couldn't parse wfslayer to json");
            throw new ServiceException(ex.getMessage());

        }
    }

    /**
     * Return the name of the geometry column for the schema of a featuretype.
     *
     * @param schema geotools wfs SimpleFeatureType
     * @return name of the geometry column
     */
    public static String getFeaturetypeGeometryName(SimpleFeatureType schema) throws ServiceException {
        try {
            return schema.getGeometryDescriptor().getName().getLocalPart();
        } catch (Exception ex) {
            throw new ServiceException("No geometry column. " + ex.getMessage());
        }
    }

    /**
     * Get the layer's title
     *
     * @param data geotools wfs SimpleFeatureType
     * @return the title of the featuretype
     */
    public static String getFeaturetypeTitle(WFSDataStore data, String typeName) {
        try {
            return data.getFeatureSource(typeName).getInfo().getTitle();
        } catch (Exception ex) {
            //log.warn("getFeaturetypeGeometryName",ex);
            return null;
        }
    }


    /**
     * WMS layer data to json
     *
     * @param schema     SimpleFeatureType
     * @param typeName
     * @return WFSLayerConfiguration  wfs feature type properties for wfs service and oskari rendering
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static WFSLayerConfiguration layerToWfsLayerConfiguration(SimpleFeatureType schema, String typeName, String rurl,
                                                                     String user, String pw)
            throws ServiceException {

        final WFSLayerConfiguration lc = new WFSLayerConfiguration();
        lc.setDefaults();


        lc.setURL(rurl);
        lc.setUsername(user);
        lc.setPassword(pw);

        lc.setLayerName(typeName);


        try {

            String[] nameParts = schema.getName().getLocalPart().split(schema.getName().getSeparator());
            String xmlns = "";
            String name = nameParts[0];
            if (nameParts.length > 1) {
                xmlns = nameParts[0];
                name = nameParts[1];
            }

            lc.setLayerId("layer_" + name);

            // fails if doesn't have a geometry column
            lc.setGMLGeometryProperty(getFeaturetypeGeometryName(schema));

            //TODO add srs check support later
            // seems this is not needed here since it isn't used,
            // but could be used for checking for valid crs so leaving it in code
            // if (schema.getCoordinateReferenceSystem() != null)
            // lc.setSRSName("EPSG:"+Integer.toString(CRS.lookupEpsgCode(schema.getCoordinateReferenceSystem(), true)));

            //lc.setGMLVersion();
            lc.setWFSVersion(DEFAULT_VERSION);
            //lc.setMaxFeatures(data.getMaxFeatures());
            lc.setFeatureNamespace(xmlns);
            lc.setFeatureNamespaceURI(schema.getName().getNamespaceURI());

            lc.setFeatureElement(name);

            return lc;
        } catch (Exception ex) {
            log.warn(ex, "Couldn't get wfs feature source data");
            throw new ServiceException(ex.getMessage());
        }

    }

    /**
     * WFS 2.0.0 layer data to json
     *
     * @param featype
     * @return WFSLayerConfiguration  wfs feature type properties for wfs service and oskari rendering
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static WFSLayerConfiguration layerToWfs20LayerConfiguration(WFS2FeatureType featype, String rurl, String user,
                                                                       String pw)
            throws ServiceException {

        final WFSLayerConfiguration lc = new WFSLayerConfiguration();
        lc.setWFS20Defaults();
        lc.setURL(rurl);
        lc.setUsername(user);
        lc.setPassword(pw);
        lc.setLayerName(featype.getTitle());

        try {

            String[] nameParts = featype.getName().split(":");
            String xmlns = "";
            String name = nameParts[0];
            if (nameParts.length > 1) {
                xmlns = nameParts[0];
                name = nameParts[1];
            }

            lc.setLayerId("layer_" + name);
            lc.setWFSVersion(WFS2_0_0_VERSION);
            lc.setFeatureNamespace(xmlns);
            if (featype.getNsUri() != null) {
                lc.setFeatureNamespaceURI(featype.getNsUri());
            }
            String geomName = featype.getGeomPropertyName();
            if (geomName != null) {
                lc.setGMLGeometryProperty(featype.getGeomPropertyName());
            }

            lc.setFeatureElement(name);
            // WFS 2.0 parser items
            lc.setTemplateName(featype.getName());
            lc.setTemplateType(featype.getTemplateType());
            lc.setResponseTemplate(featype.getResponseTemplate());
            lc.setParseConfig(featype.getParseConfig());
            lc.setRequestTemplate(featype.getRequestTemplate());


            return lc;
        } catch (Exception ex) {
            log.warn(ex, "Couldn't get wfs feature source data");
            throw new ServiceException(ex.getMessage());
        }

    }

    /**
     * WFS 1.x.0 layer data to json  in case geotools fails
     *
     * @param featype
     * @return WFSLayerConfiguration  wfs feature type properties for wfs service and oskari rendering
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static WFSLayerConfiguration layerToWfs1xLayerConfiguration(WFS2FeatureType featype, String rurl, String user,
                                                                       String pw)
            throws ServiceException {

        final WFSLayerConfiguration lc = new WFSLayerConfiguration();
        lc.setDefaults();


        lc.setURL(rurl);
        lc.setUsername(user);
        lc.setPassword(pw);

        lc.setLayerName(featype.getTitle());


        try {

            String[] nameParts = featype.getName().split(":");
            String xmlns = "";
            String name = nameParts[0];
            if (nameParts.length > 1) {
                xmlns = nameParts[0];
                name = nameParts[1];
            }

            lc.setLayerId("layer_" + name);
            String geomName = featype.getGeomPropertyName();
            if (geomName != null) {
                lc.setGMLGeometryProperty(geomName);
            }

            // Use oskari front srs
            //  lc.setSRSName(featype.getDefaultSrs());


            //lc.setGMLVersion();
            lc.setWFSVersion(DEFAULT_VERSION);
            //lc.setMaxFeatures(data.getMaxFeatures());
            lc.setFeatureNamespace(xmlns);
            if (featype.getNsUri() != null) {
                lc.setFeatureNamespaceURI(featype.getNsUri());
            }

            lc.setFeatureElement(name);

            return lc;
        } catch (Exception ex) {
            log.warn(ex, "Couldn't get wfs 1.x.0 feature source data");
            //return null;
            throw new ServiceException(ex.getMessage());
        }

    }


    /**
     * Finalise WMS service url for GetCapabilities request
     *
     * @param urlin
     * @return
     */
    public static String getUrl(String urlin, String version) {

        if (urlin.isEmpty()) {
            return "";
        }
        String url = urlin;
        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1) {
                url = url + "service=WFS";
            }
            if (url.toLowerCase().indexOf("getcapabilities") == -1) {
                url = url + "&request=GetCapabilities";
            }
        } else {
            if (url.toLowerCase().indexOf("service=") == -1) {
                url = url + "&service=WFS";
            }
            if (url.toLowerCase().indexOf("getcapabilities") == -1) {
                url = url + "&request=GetCapabilities";
            }

        }
        if (url.toLowerCase().indexOf("version") == -1) {
            url = url + "&version=" + version;
        }

        return url;
    }

    /**
     * Finalise WFS service url for DescribeFeatureType request
     *
     * @param urlin
     * @return
     */
    public static String getDescribeFeatureTypeUrl(String urlin, String version, String featureType) {

        if (urlin.isEmpty()) {
            return "";
        }
        String url = urlin;
        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1) {
                url = url + "service=WFS";
            }
            if (url.toLowerCase().indexOf("describefeaturetype") == -1) {
                url = url + "&request=DescribeFeatureType&typeNames=" + featureType;
            }
        } else {
            if (url.toLowerCase().indexOf("service=") == -1) {
                url = url + "&service=WFS";
            }
            if (url.toLowerCase().indexOf("describefeaturetype") == -1) {
                url = url + "&request=DescribeFeatureType&typeNames=" + featureType;
            }

        }
        if (url.toLowerCase().indexOf("version") == -1) {
            url = url + "&version=" + version;
        }

        return url;
    }

    /**
     * Parse DescribeFeatureType response - at least namespace uri
     * Use this for wfs 2.0.0  - use geotools datastore for wfs 1.1.0
     *
     * @param ft
     * @param data
     */
    public static void parseWfs2xDescribeFeatureType(WFS2FeatureType ft, final String data) throws ServiceException{

        try {
            // GetCapabilities request
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            // dbf.setNamespaceAware(true);  //default false
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document doc = builder.parse(new InputSource(
                    new ByteArrayInputStream(data.getBytes("UTF-8"))));

            String nsuri = doc.getDocumentElement().getAttribute("targetNamespace");

            if (nsuri != null) {
                ft.setNsUri(nsuri);
            }
            // Try to find Geometry property name
            // Get Elements
            NodeList elements = doc.getDocumentElement().getElementsByTagName("xs:element");
            if (elements.getLength() == 0) {
                elements = doc.getDocumentElement().getElementsByTagName("xsd:element");
            }
            if (elements.getLength() == 0) {
                elements = doc.getDocumentElement().getElementsByTagName("element");
            }
            if (elements.getLength() == 0) {
                elements = doc.getDocumentElement().getElementsByTagNameNS(null, "element");
            }
            // Loop elements for to get geometry property name - some services response has flat featuretype data or
            // some services returns a set of .xsd files
            String geomName = "geometry";
            for (int i = 0; i < elements.getLength(); i++) {

                Node node = elements.item(i);
                if (node instanceof Element) {
                    Element elem = (Element) elements.item(i);
                    String type = elem.getAttribute("type");
                    String name = elem.getAttribute("name");
                    if (ft.getName().split(":")[ft.getName().split(":").length-1].equals(name)) {
                        ft.setGeomPropertyName(geomName);
                        break;
                    }
                    //is geom property
                    if (GEOMTYPES.contains(type)) {
                        geomName = elem.getAttribute("name");
                    }
                }

            }

        } catch (Exception ex) {
            log.debug(ex, "WFS 2.0.0 DescribeFeaturetype failed ");
            throw new ServiceException("WFS 2.0.0 DescribeFeatureType failed");
        }
    }

    /**
     * Parse DescribeFeatureType response - at least namespace uri and geom property
     * Use this  for wfs 1.1.0, if geotools fails
     *
     * @param data describefeaturetype response
     */
    private static WFS2FeatureType parseWfs1xDescribeFeatureType(final String data, String name, WFSDataStore store) {

        WFS2FeatureType ft = new WFS2FeatureType();
        ft.setName(name);
        //ft.setDefaultSrs(srsval);  we use front side default srs

        try {
            ft.setTitle(getFeaturetypeTitle(store, name));
            // GetCapabilities request
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            // dbf.setNamespaceAware(true);  //default false
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document doc = builder.parse(new InputSource(
                    new ByteArrayInputStream(data.getBytes("UTF-8"))));

            String nsuri = doc.getDocumentElement().getAttribute("targetNamespace");

            if(nsuri == null || nsuri.isEmpty()){
                // try to get uri via well known prefix mapping
                nsuri = getPreDefinedUri(name);
            }

            if (nsuri != null) {
                ft.setNsUri(nsuri);
            }

            //Get Elements
            NodeList elements = doc.getDocumentElement().getElementsByTagName("xs:element");
            if (elements.getLength() == 0) {
                elements = doc.getDocumentElement().getElementsByTagName("xsd:element");
            }
            if (elements.getLength() == 0) {
                elements = doc.getDocumentElement().getElementsByTagName("element");
            }
            if (elements.getLength() == 0) {
                elements = doc.getDocumentElement().getElementsByTagNameNS(null, "element");
            }
            //Loop elements get geometry property name
            //Default
            ft.setGeomPropertyName(DEFAULT_GEOMETRY_NAME);

            for (int i = 0; i < elements.getLength(); i++) {

                Node node = elements.item(i);
                if (node instanceof Element) {
                    Element elem = (Element) elements.item(i);
                    String type = elem.getAttribute("type");
                    //is geom property
                    if (GEOMTYPES.contains(type)) {
                        ft.setGeomPropertyName(elem.getAttribute("name"));
                        break;
                    }
                }

            }

        } catch (Exception ex) {
            log.debug(ex, "WFS 1.1.0 DescribeFeaturetype parse failed ");
        }
        return ft;
    }

    /**
     * Set parser config items to featuretype and parser type information to layer title
     *
     * @param ft            featuretype items
     * @param parserConfigs parser configurations in oskari_wfs_parse_config table
     */
    public static void parserConfigType2Title(JSONObject feaconf, WFS2FeatureType ft, WFSParserConfigs parserConfigs,
                                              final String extraApped) {

        String type = "Unknown";
        String title = "Unknown";
        if (feaconf == null) {
            // Get default parser config
            JSONArray feaconffa = parserConfigs.getDefaultFeatureTypeConfig(ft.getNsUri(), ft.getName());
            feaconf = JSONHelper.getJSONObject(feaconffa, 0);
        }

        if (feaconf != null) {
            type = JSONHelper.getStringFromJSON(feaconf, "type", "Default Path");
            title = JSONHelper.getStringFromJSON(feaconf, "title", "Parser");
            ft.setTemplateType(type);
            ft.setResponseTemplate(JSONHelper.getStringFromJSON(feaconf, "response_template", null));
            ft.setRequestTemplate(JSONHelper.getStringFromJSON(feaconf, "request_template", null));
            JSONObject pconf = JSONHelper.getJSONObject(feaconf, "parse_config");
            if (pconf != null) {
                ft.setParseConfig(pconf.toString());
            }
        }
        ft.setTitle(ft.getTitle() + " (" + type + " " + title + " )" + extraApped);

    }

    /**
     * Scan 1st node value for to child node
     *
     * @param subnodes
     * @param name     child node name
     * @param val      child node value
     * @return node value
     */
    private static Node getChildNodes(NodeList subnodes, String name, Node val) {
        if (val != null) {
            return val;
        }
        //if(subnodes == null) return val;
        for (int k = 0; k < subnodes.getLength(); k++) {
            String localname = subnodes.item(k).getLocalName();
            if (localname == null) {
                localname = subnodes.item(k).getNodeName();
            }
            if (localname != null) {
                if (localname.equals(name)) {
                    return subnodes.item(k);
                } else if (localname.indexOf(':') > -1) {
                    //FIXME: getLocalName() above always seems to return null -> doing a manual poor man's namespace check for now...
                    localname = localname.substring(localname.indexOf(':') + 1);
                    if (localname.equals(name)) {
                        return subnodes.item(k);
                    }
                }
            }
            val = getChildNodes(subnodes.item(k).getChildNodes(), name, val);
            if (val != null) {
                return val;
            }
        }

        return val;
    }
    public static String scanChildNode(NodeList subnodes, String name, String val) {
        Node node = getChildNodes(subnodes, name, null);
        if (node != null) return node.getTextContent();
        return null;
    }
    /**
     * Scan  node values for to child node
     *
     * @param subnodes
     * @param name     child node name
     * @return node value
     */
    private static List<String> getChildNodeValues(NodeList subnodes, String name) {
        List<String> texts = new ArrayList<String>();
        //if(subnodes == null) return val;
        for (int k = 0; k < subnodes.getLength(); k++) {
            Node node = subnodes.item(k);
            String localname = node.getLocalName();
            if (localname == null) {
                localname = node.getNodeName();
            }
            if (localname != null) {
                if (localname.equals(name)) {
                    texts.add(node.getTextContent());
                }else if (localname.contains(":")) {
                    //FIXME: getLocalName() above always seems to return null -> doing a manual poor man's namespace check for now...
                    localname = localname.substring(localname.indexOf(':') + 1);
                    if (localname.equals(name)) {
                        texts.add(node.getTextContent());
                    }
                }
            }
        }

        return texts;
    }
    public static String[] scanChildNodes(NodeList subnodes, String name) {
        List <String> texts = getChildNodeValues (subnodes, name);
        return texts.toArray(new String[texts.size()]);
    }

    private static Node getChildNodesByAttributeName (NodeList nodes, String attName) {

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!node.hasAttributes()) continue;
            Node name = node.getAttributes().getNamedItem("name");
            if (name == null) continue;
            if (attName.equals(name.getTextContent())) return node;
        }
        return null;
    }

    public static String getPreDefinedUri(String name) {
        String uri = null;
        String prefix = name.split(":")[0];
        //TODO: use resource file for config
        uris.put("akaava","http://www.paikkatietopalvelu.fi/gml/asemakaava");
        uris.put("kanta","http://www.paikkatietopalvelu.fi/gml/kantakartta");
        uris.put("mkos","http://www.paikkatietopalvelu.fi/gml/opastavattiedot/osoitteet");
        uris.put("mkok","http://www.paikkatietopalvelu.fi/gml/opastavattiedot/opaskartta");
        uris.put("rakval","http://www.paikkatietopalvelu.fi/gml/rakennusvalvonta");

        uri = uris.get(prefix);

        return uri;
    }

   
}
