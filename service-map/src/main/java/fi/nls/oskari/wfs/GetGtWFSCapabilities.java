package fi.nls.oskari.wfs;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.util.WFSParserConfigs;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Methods for parsing WFS capabilities data
 * Prototype
 */
public class GetGtWFSCapabilities {

    private static final Logger log = LogFactory.getLogger(GetGtWFSCapabilities.class);

    private static final String KEY_LAYERS = "layers";
    private static final String KEY_LAYERS_WITH_ERRORS = "layersWithErrors";
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
                                                final String pw)
            throws ServiceException {
        try {
            String wfs_version = version;
            if (version.isEmpty()) {
                wfs_version = DEFAULT_VERSION;
            }
            // Only default_version and WFS2_0_0_VERSION  are supported
            if (!version.equals(WFS2_0_0_VERSION)) {
                wfs_version = DEFAULT_VERSION;
            }
            Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(rurl, wfs_version, user, pw);
            if (capa == null || (!capa.containsKey("WFSDataStore") && !capa.containsKey("FeatureTypeList"))) {
                throw new ServiceException("Couldn't read/get wfs capabilities response from url.");
            }
            try {

                return parseLayer(capa, wfs_version, rurl, user, pw);

            } catch (Exception ex) {
                throw new ServiceException("Couldn't read/get wfs capabilities response from url.", ex);
            }

        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wfs capabilities response from url.", ex);
        }
    }

    /**
     * Get all WFS layers (featuretypes) data in JSON
     *
     * @param rurl    WFS service url
     * @param version WFS service version
     * @return json of wfslayer json array
     */
    public static Map<String, Object> getGtDataStoreCapabilities(final String rurl, final String version, String user,
                                                                 String pw) {
        if (version.equals(WFS2_0_0_VERSION)) {
            return getGtDataStoreCapabilities_2_x(rurl, version, user, pw);
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
                                                                     String user, String pw) {

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
            if (data instanceof WFSDataStore) {
                wfsds = (WFSDataStore) data;
            }
            if (wfsds != null) {
                capabilities.put("status", "OK");
                capabilities.put("WFSDataStore", wfsds);
            } else {
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
     * Get WFS GetCapabilities data for WFS versions 2.x
     * There is no support for wfs2 WFSDataStore in geotools
     * Use gt-wfs-ng/WFSClient in the future for to parse Capabilities
     *
     * @param rurl    WFS service url
     * @param version WFS service version
     * @return HashMap / HashMap of wfs service featuretypes
     */
    public static Map<String, Object> getGtDataStoreCapabilities_2_x(final String rurl, final String version,
                                                                     String user, String pw) {
        WFSParserConfigs parseConfigs = new WFSParserConfigs();
        Map<String, Object> capabilities = new HashMap<String, Object>();
        try {
            // GetCapabilities request
            String data = IOHelper.getURL(getUrl(rurl, version), user, pw);
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
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
            Map<String, _FeatureType> featuretypes = new HashMap<String, _FeatureType>();
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
                String[] otherSrsval  = scanChildNodes(featypes.item(i).getChildNodes(), OtherSrs);

                if (nameval != null) {
                    if (titleval == null) {
                        titleval = nameval;
                    }
                    _FeatureType tmpft = new _FeatureType();
                    tmpft.setName(nameval);
                    tmpft.setTitle(titleval);
                    tmpft.setDefaultSrs(srsval);
                    if(otherSrsval.length > 0){
                        tmpft.setOtherSrs(otherSrsval);
                    }
                    // Try to parse describe feature type response of wfs 2.0.0 service at least namespaceUri
                    parseWfs2xDescribeFeatureType(tmpft, IOHelper.getURL(getDescribeFeatureTypeUrl(rurl, version, nameval), user, pw));
                    // Append parser type to title
                    parserConfigType2Title(tmpft, parseConfigs);
                    featuretypes.put(nameval, tmpft);
                }

            }
            if (featypes.getLength() > 0) {
                capabilities.put("status", "OK");
                capabilities.put("FeatureTypeList", featuretypes);
            } else {
                capabilities.put("status", "FAILED");
                capabilities.put("exception", "No featuretypes found - url: " + rurl);
            }
        } catch (Exception ex) {
            capabilities.put("status", "FAILED");
            capabilities.put("exception", ex.getMessage());
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
        if (capa.containsKey("WFSDataStore")) {
            WFSDataStore data = (WFSDataStore) capa.get("WFSDataStore");
            return parseWfs1xLayer(data, version, rurl, user, pw);
        } else if (capa.containsKey("FeatureTypeList")) {
            Map<String, _FeatureType> data = (Map<String, _FeatureType>) capa.get("FeatureTypeList");
            return parseWfs2xLayer(data, version, rurl, user, pw);
        }
        return null;

    }

    /**
     * Parse supported crs Projections  wfs 1.1.0 or wfs 2.0.0
     *
     * @param capa  Object WFS datastore or FeatureTypeList
     * @param version
     * @param name
     * @return
     */
    public static Set<String> parseProjections(Map<String, Object> capa, String version, String name)
    {
        if (capa == null) {
            return null;
        }
        if (capa.containsKey("WFSDataStore")) {
            WFSDataStore data = (WFSDataStore) capa.get("WFSDataStore");
            return parseWfs1xProjections(data, version, name);
        } else if (capa.containsKey("FeatureTypeList")) {
            Map<String, _FeatureType> data = (Map<String, _FeatureType>) capa.get("FeatureTypeList");
            return parseWfs2xProjections(data, version, name);
        }
        return null;

    }

    /**
     * Parse layer wfs 2.x
     *
     * @param typeNames capabilites typenames
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static JSONObject parseWfs2xLayer(Map<String, _FeatureType> typeNames, String version, String rurl,
                                             String user, String pw)
            throws ServiceException {
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

            // Loop feature types
            for (Map.Entry<String, _FeatureType> entry : typeNames.entrySet()) {
                String typeName = entry.getKey();
                _FeatureType fea2x = entry.getValue();
                try {
                    JSONObject temp = layerToOskariLayerJson(fea2x, version, typeName, rurl, user, pw);
                    if (temp != null) {
                        layers.put(temp);
                    }
                } catch (ServiceException se) {
                    JSONObject errorLayer = new JSONObject();
                    JSONHelper.putValue(errorLayer, "title", fea2x.getTitle());
                    JSONHelper.putValue(errorLayer, "layerName", typeName);
                    JSONHelper.putValue(errorLayer, "errorMessage", se.getMessage());
                    layersWithErrors.put(errorLayer);

                }
            }

            return wfsLayers;

        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wfs 2.x capabilities layer", ex);
        }
    }

    /**
     * Parse layer wfs 1.x
     *
     * @param data geotools wfs DataStore
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static JSONObject parseWfs1xLayer(WFSDataStore data, String version, String rurl, String user, String pw)
            throws ServiceException {
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
                        JSONObject temp = layerToOskariLayerJson(data, version, typeName, rurl, user, pw);
                        if (temp != null) {
                            layers.put(temp);
                        }
                    } catch (ServiceException se) {
                        JSONObject errorLayer = new JSONObject();
                        JSONHelper.putValue(errorLayer, "title", getFeaturetypeTitle(data, typeName));
                        JSONHelper.putValue(errorLayer, "layerName", typeName);
                        JSONHelper.putValue(errorLayer, "errorMessage", se.getMessage());
                        layersWithErrors.put(errorLayer);

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
    public static Set<String> parseWfs1xProjections(WFSDataStore data, String version, String typeName) {
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
    public static Set<String> parseWfs2xProjections(Map<String, _FeatureType> typeNames, String version, String typeName) {

        if (typeNames == null || typeName == null) {
            return null;
        }
        Set<String> crss = new HashSet<String>();
        try {

            _FeatureType  featureType = typeNames.get(typeName);
             crss.add(ProjectionHelper.shortSyntaxEpsg(featureType.defaultSrs));
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
    public static JSONObject layerToOskariLayerJson(Object capa, String version, String typeName, String rurl,
                                                    String user, String pw)
            throws ServiceException {

        final OskariLayer oskariLayer = new OskariLayer();
        oskariLayer.setType(OskariLayer.TYPE_WFS);
        oskariLayer.setUrl(rurl);
        // THIS IS ON PURPOSE: min -> max, max -> min
        oskariLayer.setMaxScale(1d);
        oskariLayer.setMinScale(1500000d);
        oskariLayer.setName(typeName);
        oskariLayer.setVersion(version);
        String title = "";

        WFSDataStore data1x = null;
        _FeatureType fea2x = null;
        _FeatureType fea1x = null;

        try {
            if (capa instanceof WFSDataStore) {
                data1x = (WFSDataStore) capa;
                SimpleFeatureType schema = getSchema(data1x, typeName);
                if (schema != null) {
                    //check whether there actually is a geometry column -> otherwise don't go further.
                    if (GetGtWFSCapabilities.getFeaturetypeGeometryName(schema) == null) {
                        throw new ServiceException("No geometry column.");
                    }

                    // Source
                    FeatureSource<SimpleFeatureType, SimpleFeature> source = data1x.getFeatureSource(typeName);
                    title = source.getInfo().getTitle();

                } else {
                    // try own DescribeFeature request and parsing
                    fea1x = parseWfs1xDescribeFeatureType(IOHelper.getURL(getDescribeFeatureTypeUrl(rurl, version, typeName), user, pw), typeName, data1x);
                    title = fea1x.getTitle();
                    //check whether there actually is a geometry column -> otherwise don't go further.
                    if (fea1x.getGeomPropertyName() == null) {
                        throw new ServiceException("No geometry column.");
                    }

                }
            } else if (capa instanceof _FeatureType) {
                fea2x = (_FeatureType) capa;
                title = fea2x.getTitle();
            }

            // setup UI names for all supported languages
            final String[] languages = PropertyUtil.getSupportedLanguages();
            for (String lang : languages) {
                oskariLayer.setName(lang, title);
            }

        } catch (Exception ex) {
            log.warn(ex, "Couldn't get wfs feature source data");
            throw new ServiceException(ex.getMessage());
        }

        try {

            JSONObject json = FORMATTER.getJSON(oskariLayer, PropertyUtil.getDefaultLanguage(), false);
            // add/modify admin specific fields
            OskariLayerWorker.modifyCommonFieldsForEditing(json, oskariLayer);
            // for admin ui only
            JSONHelper.putValue(json, "title", title);
            //FIXME  merge oskarilayer and wfsLayer
            WFSLayerConfiguration lc = null;
            if (data1x != null && fea1x != null) {
                // WFS 1.x  Geotools parse FAILED
                lc = GetGtWFSCapabilities.layerToWfs1xLayerConfiguration(fea1x, rurl, user, pw);
            } else if (data1x != null && fea1x == null) {
                // WFS 1.x  Geotools parse OK
                lc = GetGtWFSCapabilities.layerToWfsLayerConfiguration(data1x, typeName, rurl, user, pw);
            } else if (fea2x != null) {
                //WFS 2.0
                lc = GetGtWFSCapabilities.layerToWfs20LayerConfiguration(fea2x, rurl, user, pw);
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
     * @return name of the geometry column if exists, null otherwise.
     */
    public static String getFeaturetypeGeometryName(SimpleFeatureType schema) {
        try {
            return schema.getGeometryDescriptor().getName().getLocalPart();
        } catch (Exception ex) {
//            log.warn("getFeaturetypeGeometryName",ex);
            return null;
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
     * @param data     geotools wfs DataStore
     * @param typeName
     * @return WFSLayerConfiguration  wfs feature type properties for wfs service and oskari rendering
     * @throws fi.nls.oskari.service.ServiceException
     */
    public static WFSLayerConfiguration layerToWfsLayerConfiguration(WFSDataStore data, String typeName, String rurl,
                                                                     String user, String pw)
            throws ServiceException {

        final WFSLayerConfiguration lc = new WFSLayerConfiguration();
        lc.setDefaults();


        lc.setURL(rurl);
        lc.setUsername(user);
        lc.setPassword(pw);

        lc.setLayerName(typeName);


        try {
            SimpleFeatureType schema = data.getSchema(typeName);

            String[] nameParts = schema.getName().getLocalPart().split(schema.getName().getSeparator());
            String xmlns = "";
            String name = nameParts[0];
            if (nameParts.length > 1) {
                xmlns = nameParts[0];
                name = nameParts[1];
            }

            lc.setLayerId("layer_" + name);

            // Geometry property
            String geomName = getFeaturetypeGeometryName(schema);//schema.getGeometryDescriptor().getName().getLocalPart();
            //TODO add srs check support later
            // seems this is not needed here since it isn't used,
            // but could be used for checking for valid crs so leaving it in code
            // if (schema.getCoordinateReferenceSystem() != null)
            // lc.setSRSName("EPSG:"+Integer.toString(CRS.lookupEpsgCode(schema.getCoordinateReferenceSystem(), true)));


            lc.setGMLGeometryProperty(geomName);


            //lc.setGMLVersion();
            lc.setWFSVersion(data.getInfo().getVersion());
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
    public static WFSLayerConfiguration layerToWfs20LayerConfiguration(_FeatureType featype, String rurl, String user,
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
    public static WFSLayerConfiguration layerToWfs1xLayerConfiguration(_FeatureType featype, String rurl, String user,
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


            lc.setGMLGeometryProperty(featype.getGeomPropertyName());
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
    public static void parseWfs2xDescribeFeatureType(_FeatureType ft, final String data) {

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

        } catch (Exception ex) {
            log.debug(ex, "WFS 2.0.0 DescribeFeaturetype failed ");
        }
    }

    /**
     * Parse DescribeFeatureType response - at least namespace uri and geom property
     * Use this  for wfs 1.1.0, if geotools fails
     *
     * @param data describefeaturetype response
     */
    private static _FeatureType parseWfs1xDescribeFeatureType(final String data, String name, WFSDataStore store) {

        _FeatureType ft = new _FeatureType();
        ft.setName(name);
        //ft.setDefaultSrs(srsval);  we use front side default srs

        try {
            ft.setTitle(store.getFeatureTypeTitle(name));
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
    public static void parserConfigType2Title(_FeatureType ft, WFSParserConfigs parserConfigs) {

        JSONArray feaconf = parserConfigs.getFeatureTypeConfig(ft.getName());
        String type = "Unknown";
        if (feaconf == null) {
            // Get default parser config
            feaconf = parserConfigs.getDefaultFeatureTypeConfig(ft.getNsUri(), ft.getName());
        }

        if (feaconf != null) {
            type = JSONHelper.getStringFromJSON(JSONHelper.getJSONObject(feaconf, 0), "type", "Default Path");
            ft.setTemplateType(type);
            ft.setResponseTemplate(JSONHelper.getStringFromJSON(JSONHelper.getJSONObject(feaconf, 0), "response_template", null));
            ft.setRequestTemplate(JSONHelper.getStringFromJSON(JSONHelper.getJSONObject(feaconf, 0), "request_template", null));
            JSONObject pconf = JSONHelper.getJSONObject(JSONHelper.getJSONObject(feaconf, 0), "parse_config");
            if (pconf != null) {
                ft.setParseConfig(pconf.toString());
            }
        }
        ft.setTitle(ft.getTitle() + " (" + type + " parser)");

    }

    /**
     * Scan 1st node value for to child node
     *
     * @param subnodes
     * @param name     child node name
     * @param val      child node value
     * @return node value
     */
    public static String scanChildNode(NodeList subnodes, String name, String val) {
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
                    return subnodes.item(k).getTextContent();
                } else if (localname.indexOf(':') > -1) {
                    //FIXME: getLocalName() above always seems to return null -> doing a manual poor man's namespace check for now...
                    localname = localname.substring(localname.indexOf(':') + 1);
                    if (localname.equals(name)) {
                        return subnodes.item(k).getTextContent();
                    }
                }
            }
            val = scanChildNode(subnodes.item(k).getChildNodes(), name, val);
            if (val != null) {
                return val;
            }
        }

        return val;
    }
    /**
     * Scan  node values for to child node
     *
     * @param subnodes
     * @param name     child node name
     * @return node value
     */
    public static String[] scanChildNodes(NodeList subnodes, String name) {
        List<String> texts = new ArrayList<String>();
        //if(subnodes == null) return val;
        for (int k = 0; k < subnodes.getLength(); k++) {
            String localname = subnodes.item(k).getLocalName();
            if (localname == null) {
                localname = subnodes.item(k).getNodeName();
            }
            if (localname != null) {
                if (localname.equals(name)) {
                    texts.add(subnodes.item(k).getTextContent());
                }
            }
        }

        return texts.toArray(new String[texts.size()]);
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

    public static class _FeatureType {
        private String name;
        private String title;
        private String defaultSrs;
        private String[] OtherSrs;
        private String nsUri;
        private String templateDescription;
        private String templateType;
        private String requestTemplate;
        private String responseTemplate;
        private String parseConfig;
        private String geomPropertyName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDefaultSrs() {
            return defaultSrs;
        }

        public void setDefaultSrs(String defaultSrs) {
            this.defaultSrs = defaultSrs;
        }

        public String[] getOtherSrs() {
            return OtherSrs;
        }

        public void setOtherSrs(String[] otherSrs) {
            this.OtherSrs = otherSrs;
        }

        public String getNsUri() {
            return nsUri;
        }

        public void setNsUri(String nsUri) {
            this.nsUri = nsUri;
        }


        public String getTemplateDescription() {
            return templateDescription;
        }

        public void setTemplateDescription(String templateDescription) {
            this.templateDescription = templateDescription;
        }

        public String getTemplateType() {
            return templateType;
        }

        public void setTemplateType(String templateType) {
            this.templateType = templateType;
        }

        public String getRequestTemplate() {
            return requestTemplate;
        }

        public void setRequestTemplate(String requestTemplate) {
            this.requestTemplate = requestTemplate;
        }

        public String getResponseTemplate() {
            return responseTemplate;
        }

        public void setResponseTemplate(String responseTemplate) {
            this.responseTemplate = responseTemplate;
        }

        public String getParseConfig() {
            return parseConfig;
        }

        public void setParseConfig(String parseConfig) {
            this.parseConfig = parseConfig;
        }

        public String getGeomPropertyName() {
            return geomPropertyName;
        }

        public void setGeomPropertyName(String geomPropertyName) {
            this.geomPropertyName = geomPropertyName;
        }
    }

}
