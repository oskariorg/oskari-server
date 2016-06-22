package fi.nls.oskari.wfs;

import fi.nls.oskari.domain.map.wfs.WFS2FeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.WFSDescribeFeatureHelper;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.Classes;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;

import java.io.*;
import java.util.*;

/**
 * @author Oskari team
 */
public class WFSServiceTester {

    private static Logger log = LogFactory.getLogger(WFSServiceTester.class);
    private final static String  KEY_LAYERS = "layers";
    private final static String  PROP_PROXYHOST = "http.proxyHost";
    private final static String  PROP_PROXYPORT = "http.proxyPort";
    private final static String  PROP_NONPROXYHOSTS = "http.nonproxyHosts";
    private final static String  PROP_WFS_SQL_INSERT_TEMPLATE = "template_WFS_layer_insert.sql";
    private final static String  WRITE_TEMPLATE_PATH = "wfs.sql.write.path";
    private final static String  EXCEPTION_REPORT = "EXCEPTIONREPORT";
    private final static String LAYER_GROUP = System.getProperty("oskari.layer.group");
    private final static String INSPIRE_THEME = System.getProperty("oskari.inspire.theme");
    private final static String WPS_PARAMS = System.getProperty("oskari.wps_params");
    private final static String LAYER_OPACITY = System.getProperty("oskari.layer.opacity");

    private final static String[] versions =  {"1.0.0", "1.1.0", "2.0.0"};
    private static StringBuilder report = new StringBuilder();

    public static void main(String[] args) throws Exception {


        PropertyUtil.loadProperties("/service-wfs.properties");

        // replace logger after properties are populated
        log = LogFactory.getLogger(WFSServiceTester.class);

        Locale l = new Locale("fi");
        Date dte = Calendar.getInstance(l).getTime();

        final String url = System.getProperty("wfs.service.url");

        report.append("++++++++++++++++++++++++++++++++++++++++++++++++ WFS Test START - " + dte.toString() + "\n");

        // Get args
        if(url == null)
        {
            info("incorrect parameters ");
            info("use - mvn  exec:java -Dwfs.service.url=http://geo.stat.fi:8080/geoserver/wfs [wfs.service.user= wfs.service.pw= wfs.service.epsg=] ");
            System.exit(1);
        }

        // WFS service url e.g. http://geo.stat.fi:8080/geoserver/wfs



        final String user = System.getProperty("wfs.service.user");
        final String pw = System.getProperty("wfs.service.pw");
        final String epsg = System.getProperty("wfs.service.epsg");

        setProxy();  // IDEA global proxy setup does'n work - you could use alternatively VM options in idea run / edit configuration

        info("Service url: " + url);

        for (String version : versions) {
            // Test version by version
            info("WFS test - version: " + version + ":::: START <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");
            TestWfs(url, version, user, pw, epsg);
            info("WFS test - version: " + version + ":::: END >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");

        }

        report.append("++++++++++++++++++++++++++++++++++++++++++++++++++++++++ WFS Test END - " + dte.toString());
        System.out.println(report.toString());

    }


    /**
     * Test Wfs operations as version based
     * If epsg is not defined by the user, then the wfs service default epsg is used
     * @param serviceUrl  Wfs service url
     * @param version  Wfs service version
     * @param user  credentials for the wfs service
     * @param pw
     * @param epsg  spatial referenece system code - default epsg is used, if null
     */
    public static void TestWfs(String serviceUrl, String version, String user, String pw,
                               String epsg) {

        info("WFS version test  (GetCapabilities http request) <<<< HTTP GETCAPABILITIES -------");

        TestWfsVersion(serviceUrl, version, user, pw);
        info("----------------------------------------------------------------------------- >>>>");

        info("WFS version test  (GetCapabilities GeoTools ) <<<< GEOTOOLS GETCAPABILITIES AND DESCRIBEFEATURE ----------");

        Map<String, Object> capa = TestWfsVersionGT(serviceUrl, version, user, pw, epsg);
        info("----------------------------------------------------------------------------- >>>>");

        if (capa.get("status").toString().equals("OK"))
        {
            info("WFS DescribeFeature test  (Http request ) <<<< HTTP DESCRIBEFEATURE ----------");

            TestWfsDescribeFeatureTypes(capa, serviceUrl, version, user, pw);
            info("----------------------------------------------------------------------------- >>>>");

            info("WFS GetFeature test  (Http request, Transport, Geotools ) <<<< GETFEATURE ----------");
            TestWfsGetFeatures(capa, serviceUrl, version, user, pw, epsg);
            info("----------------------------------------------------------------------------- >>>>");
        }




    }

    /**
     * Test Wfs version support
     * @param serviceUrl  Wfs service url
     */
    public static void TestWfsVersion(String serviceUrl, String version, String user, String pw) {


        String url = GetGtWFSCapabilities.getUrl(serviceUrl, version);
        try {
            final String response = IOHelper.getURL(url, user, pw);
            if (response == null) {
                info("WFS Http GetCapabilities request FAILED - version " + version);
            } else if (response.toUpperCase().indexOf(EXCEPTION_REPORT) > -1) {
                info("WFS Http GetCapabilities request FAILED - version: " + version);
                info("Exception report: " + response);
            }
            else {
                info("WFS Http GetCapabilities request OK - version " + version);
            }
        } catch (Exception ex) {
            info("WFS Http GetCapabilities request FAILED - version " + version);
        }


    }

    /**
     * Test Wfs version support og GeoTools
     * @param serviceUrl  Wfs service url
     * @return capabilites  if Map<"status"><"OK">, then there is Map<"WFSDataStore"><WFSDataStore>
     */
    public static Map<String, Object> TestWfsVersionGT(String serviceUrl, String version, String user,
                                                       String pw, String currentCrs) {


        Map<String, Object> capabilities = GetGtWFSCapabilities.getGtDataStoreCapabilities(serviceUrl, version, user,
                                                                                           pw, currentCrs);
        try {

            if ( capabilities == null) {
                info("WFS GT GetCapabilities request FAILED - version " + version);
            } else if (capabilities.get("status").toString().equals("FAILED")) {
                info("WFS GT GetCapabilities request FAILED - version: " + version);
                info("Exception report: " + capabilities.get("exception").toString());
            }
            else {
                info("WFS GT GetCapabilities request OK - version " + version);
            }
        } catch (Exception ex) {
            info("WFS GT GetCapabilities request FAILED - version " + version);
        }

        return capabilities;

    }


    public static void TestWfsDescribeFeatureTypes( Map<String, Object> capa, String serviceUrl, String version, String user, String pw) {

        if (capa == null)  return;
        if (capa.containsKey("WFSDataStore")) {

            try {
                WFSDataStore wfsds = (WFSDataStore) capa.get("WFSDataStore");

                // Feature types
                String[] typeNames = wfsds.getTypeNames();
                int count = 0;


                // Loop feature types
                for (String typeName : typeNames) {
                    count++;
                    WFSLayerConfiguration lc = GetGtWFSCapabilities.layerToWfsLayerConfiguration(wfsds, typeName, serviceUrl, user, pw);
                    TestWfsDescribeFeatureType(lc, version, count);
                }

            } catch (Exception ex) {

            }
        }
        else if (capa.containsKey("FeatureTypeList")) {

            try {
                // Feature types
                Map<String,ArrayList<WFS2FeatureType>> typeNames = (HashMap<String,ArrayList<WFS2FeatureType>>) capa.get("FeatureTypeList");

                int count = 0;

                Iterator it = typeNames.keySet().iterator();
                ArrayList<WFS2FeatureType> feaList = null;

                while (it.hasNext()) {
                    String key = it.next().toString();
                    feaList = typeNames.get(key);
                    if (feaList != null) {
                        for (WFS2FeatureType featype: feaList) {
                            String typeName = featype.getName();
                            count++;
                            WFSLayerConfiguration lc = GetGtWFSCapabilities.layerToWfs20LayerConfiguration(featype, serviceUrl, user, pw);
                            TestWfsDescribeFeatureType(lc, version, count);
                        }
                    }
                }

            } catch (Exception ex) {
                info("WFS 2.0.0 featuretypelist error",ex);
            }

        }




}

    public static JSONObject TestWfsDescribeFeatureType(WFSLayerConfiguration lc, String version, int count) {

        String layer_id = "temp_" + Integer.toString(count);
        JSONObject response = null;
        try {
            response = WFSDescribeFeatureHelper.getWFSFeaturePropertyTypes(lc, layer_id);
            if (response == null) {
                info("WFS DescribeFeatureType request FAILED - version " + version + " --  Feature: " + lc.getFeatureNamespace() + ":" + lc.getFeatureElement()+ " - default "+ lc.getSRSName());
            } else {
                info("WFS DescribeFeatureType request OK - version " + version + " --  Feature: " + lc.getFeatureNamespace() + ":" + lc.getFeatureElement()+ " - default: "+ lc.getSRSName()+ "- geomproperty: "+lc.getGMLGeometryProperty());
            }
        } catch (Exception ex) {
            info("WFS GetCapabilities request FAILED - version " + version + " --  Feature: " + lc.getFeatureNamespace() + ":" + lc.getFeatureElement()+ " - default  "+ lc.getSRSName());
        }
        return response;

    }

    public static void TestWfsGetFeatures( Map<String, Object> capa, String serviceUrl, String version, String user, String pw, String epsg) {

        if (capa == null)  return;
        if (!capa.containsKey("WFSDataStore")) return;

        try {
            WFSDataStore wfsds = (WFSDataStore) capa.get("WFSDataStore");

            // Feature types
            String[] typeNames = wfsds.getTypeNames();
            int count = 0;


            // Loop feature types
            for (String typeName : typeNames) {
                try {
                    count++;
                    WFSLayerConfiguration lc = GetGtWFSCapabilities.layerToWfsLayerConfiguration(wfsds, typeName, serviceUrl, user, pw);
                    if(epsg != null) lc.setSRSName(epsg);   // test request epsg entered by the user
                    // Write wfs layer insert scripts (sql) - only for Wfs version 1.1.0
                    String title = wfsds.getFeatureTypeTitle(typeName);
                    writeWFSSqlInsertScript(lc, title);
                    // test http GET GetFeature
                    final String response = TestGETWfsGetFeature(lc, version, count);
                    // Test Transport parser
                    double[] refPoint = TestTransportParser(lc, response, getWFSPropertieslikeOskari(wfsds, typeName));
                    // test GeoTools Bbox filter
                    if (refPoint[0] > 0d) TestGtBBOXFilter(wfsds, typeName, lc, refPoint);
                    // test http POST GetFeature BBOX  query
                } catch (Exception ex2) {

                }

            }

        } catch (Exception ex) {

        }

    }

    public static String TestGETWfsGetFeature(WFSLayerConfiguration lc, String version, int count) {

        String response = null;
        String url = WFSDescribeFeatureHelper.getGetFeatureUrl(lc.getURL() , version, lc.getFeatureNamespace(), lc.getFeatureElement(), "10");
        try {
            response = IOHelper.getURL(url, lc.getUsername(), lc.getPassword());
            if (response == null) {
                info("WFS http GET GetFeature request FAILED - version " + version);
            } else if (response.toUpperCase().indexOf(EXCEPTION_REPORT) > -1) {
                info("WFS http GET GetFeature request request FAILED - version: " + version);
                info("Exception report: " + response);
            }
            else {
                int ind = response.indexOf("featureMember");
                ind = (ind > -1) ? ind : 0;

                info("WFS http GET GetFeature request OK - version " + version + " - response... " + response.substring(ind,ind + 160));
            }
        } catch (Exception ex) {
            info("WFS http GET GetFeature request FAILED - version " + version);
        }
        return response;
    }

    public static double [] TestTransportParser(WFSLayerConfiguration lc, String response, String properties) {

        double [] refPoint = {0d, 0d};
        try {
            WFSLayerStore layer = WFSLayerStore.setJSON(lc.getAsJSON());
            // * in the config marks the default geometry
            // should contain whole schema or at least the selectedFeatureParams (+ GEOMETRY)
            //layer.addFeatureType("default", "fi_nimi:String,fi_osoite:String,postinumero:String,*the_geom:Point");
            layer.addFeatureType("default", properties);
            StringReader sr= new StringReader(response);
            BufferedReader bresponse = new BufferedReader(sr);
            // parse
            WFSParser parser = new WFSParser(bresponse, layer);
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = parser.parse();
            if (features == null || features.isEmpty()) {
                info("Transport WFSParser FAILED - version " + lc.getWFSVersion());
            }
            else {
                info("Transport WFSParser  OK - version " + lc.getWFSVersion() +  " --  Feature: " + lc.getFeatureNamespace() + ":" + lc.getFeatureElement()+ " "+ lc.getSRSName());
                refPoint = features.getBounds().getLowerCorner().getCoordinate();
                info(" Features corner: " + "1st: " + Double.toString(refPoint[0])+ " 2nd: " + Double.toString(refPoint[1]));

            }
        } catch (Exception ex) {
            info("Transport WFSParser  FAILED - version " + lc.getWFSVersion());
        }
        return refPoint;
    }

    public static void TestGtBBOXFilter(WFSDataStore data, String typeName, WFSLayerConfiguration lc, double [] refPoint) {


        try {
            SimpleFeatureType schema = data.getSchema( typeName );
            FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );


            String geomName = lc.getGMLGeometryProperty();
            double delta = 50d;  // unit m
            // if geographical coordinates
            if(refPoint[0] > -181d && refPoint[0] < 181d){
                delta = 0.005d;
            }

            ReferencedEnvelope bbox = new ReferencedEnvelope( refPoint[0]-delta, refPoint[1]-delta, refPoint[0] + delta, refPoint[1] + delta, schema.getCoordinateReferenceSystem() );
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
            Object polygon = JTS.toGeometry(bbox);
            Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );

            Query query = new Query( typeName, filter, new String[]{ geomName } );
            query.setCoordinateSystem(schema.getCoordinateReferenceSystem());
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );


            if (features == null || features.isEmpty()) {
                info("GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version " + lc.getWFSVersion());
            }
            else {
                info("GeoTools BBOX GetFeature parser OK- version " + lc.getWFSVersion() +  " --  Feature: " + lc.getFeatureNamespace() + ":" + lc.getFeatureElement()+ " "+ lc.getSRSName());
                info(" Features found: " + Integer.toString(features.size()));

            }
        } catch (Exception ex) {
            info("GeoTools BBOX GetFeature parser FAILED - version " + lc.getWFSVersion());
        }

    }

    /**
     *
     * @param data
     * @param typeName
     * @return properties  e.g.  "fi_nimi:String,fi_osoite:String,postinumero:String,*the_geom:Point"
     */
    public static String getWFSPropertieslikeOskari(WFSDataStore data, String typeName) {
        //  "fi_nimi:String,fi_osoite:String,postinumero:String,*the_geom:Point"
        StringBuilder properties = new StringBuilder();
        try {
            SimpleFeatureType schema = data.getSchema(typeName);
            // Geometry property
            String geomName = schema.getGeometryDescriptor().getName().getLocalPart();

            List<AttributeDescriptor> attr = schema.getAttributeDescriptors();
            for (AttributeDescriptor att : attr) {
                if (!geomName.equals(att.getLocalName())) {
                    properties.append(att.getLocalName());
                    properties.append(":");
                    String type = Classes.getShortName(att.getType().getBinding());
                    properties.append(type);
                    properties.append(",");
                }

            }


            properties.append("*");
            properties.append(geomName);
            properties.append(":");
            String type = Classes.getShortName(schema.getGeometryDescriptor().getType().getBinding());
            properties.append(type);

        } catch (Exception ex) {

        }
        return properties.toString();
    }
    public static void setProxy() {

        Properties systemProperties = System.getProperties();
        if(PropertyUtil.get(PROP_PROXYHOST) != null ) systemProperties.setProperty(PROP_PROXYHOST,PropertyUtil.get(PROP_PROXYHOST));
        if(PropertyUtil.get(PROP_PROXYPORT) != null ) systemProperties.setProperty(PROP_PROXYPORT,PropertyUtil.get(PROP_PROXYPORT));
        if(PropertyUtil.get(PROP_NONPROXYHOSTS) != null ) systemProperties.setProperty(PROP_NONPROXYHOSTS,PropertyUtil.get(PROP_NONPROXYHOSTS));
    }

    public static void writeWFSSqlInsertScript(WFSLayerConfiguration lc, String layerTitle) {


        if (PropertyUtil.get(PROP_WFS_SQL_INSERT_TEMPLATE) != null && lc.getWFSVersion().equals("1.1.0")) {

            // Read template
            String sqlTemplate = null;

                try {
                    sqlTemplate = getTemplate(PROP_WFS_SQL_INSERT_TEMPLATE);
                } catch (IOException e) {
                    info("WFS insert sql template close FAILED " + " --  template file: " + PROP_WFS_SQL_INSERT_TEMPLATE);
                }



            // Replace properties in template
            /*
            -- $WFS_URL  wfs service url (e.g. http://geo.stat.fi:8080/geoserver/ows)
            -- $LAYER_GROUP administrative layer group (e.g. Tilastokeskus)
            -- $LAYER_NAME
            -- $OPACITY   (0 -100)
            -- $MIN_SCALE (smallest scale e.g. 1500000  -- means !:1500000)
            -- $MAX_SCALE (largest scale e.g. 1 --> means 1:1)
            -- $LAYER_NAME_FI
            -- $LAYER_NAME_SV
            -- $LAYER_NAME_EN
            -- $INSPIRE_THEME  inspire theme  (e.g. Tilastointiyksiköt)
            -- $GEOMETRY_PROPERTY  WFS feature default geometry property name (e.g. the_geom)
            -- $GML_VERSION  e.g. 3.1.1
            -- $WFS_VERSION  e.g. 1.1.0
            -- $MAXFEATURES maximum allowed count of features to select in one request - e.g. 20000
            -- $NAMESPACE_PREFIX  namespace prefix for $FEATURE_ELEMENT  ( e.g. vaestoruutu)
            -- $EPSG  spatial reference system code (e.g. EPSG:3067)
            -- $FEATURE_ELEMENT  name of featuretype
            -- $NAMESPACE_URI namespace uri of $FEATURE_ELEMENT
            -- WPS_PARAMS if no data values must be handled json string format  e.g.{"no_data":-1}
             */
            String inspireTheme = "Inspire Theme";
            if(INSPIRE_THEME != null) inspireTheme= INSPIRE_THEME;
            String layerGroup = "Layer Group";
            if(LAYER_GROUP != null) layerGroup= LAYER_GROUP;
            String wps_params = "{}";
            if(WPS_PARAMS != null) wps_params= WPS_PARAMS;
            String opacity = "80";
            if(LAYER_OPACITY != null) opacity= LAYER_OPACITY;
            if (sqlTemplate != null) {
            sqlTemplate = sqlTemplate.replace("$WFS_URL", lc.getURL());
            sqlTemplate = sqlTemplate.replace("$LAYER_NAME", lc.getLayerName().replace(":","_"));
            sqlTemplate = sqlTemplate.replace("$LAYER_GROUP", layerGroup);
            sqlTemplate = sqlTemplate.replace("$OPACITY", opacity);
            sqlTemplate = sqlTemplate.replace("$MIN_SCALE", Double.toString(lc.getMinScale()));
            sqlTemplate = sqlTemplate.replace("$MAX_SCALE", Double.toString(lc.getMaxScale()));

            sqlTemplate = sqlTemplate.replace("$FI_LAYER_NAME", lc.getFeatureElement().substring(0,1).toUpperCase()+lc.getFeatureElement().substring(1));
            sqlTemplate = sqlTemplate.replace("$SV_LAYER_NAME", lc.getFeatureElement().substring(0,1).toUpperCase()+lc.getFeatureElement().substring(1));
            sqlTemplate = sqlTemplate.replace("$EN_LAYER_NAME", lc.getFeatureElement().substring(0,1).toUpperCase()+lc.getFeatureElement().substring(1));
            sqlTemplate = sqlTemplate.replace("$FI_LAYER_TITLE", layerTitle);
            sqlTemplate = sqlTemplate.replace("$SV_LAYER_TITLE", layerTitle);
            sqlTemplate = sqlTemplate.replace("$EN_LAYER_TITLE", layerTitle);
            sqlTemplate = sqlTemplate.replace("$INSPIRE_THEME", inspireTheme);
            sqlTemplate = sqlTemplate.replace("$GEOMETRY_PROPERTY", lc.getGMLGeometryProperty());
            sqlTemplate = sqlTemplate.replace("$GML_VERSION", lc.getGMLVersion());
            sqlTemplate = sqlTemplate.replace("$WFS_VERSION", lc.getWFSVersion());
            sqlTemplate = sqlTemplate.replace("$MAXFEATURES", Integer.toString(lc.getMaxFeatures()));
            sqlTemplate = sqlTemplate.replace("$NAMESPACE_PREFIX", lc.getFeatureNamespace());
            sqlTemplate = sqlTemplate.replace("$EPSG", lc.getSRSName());
            sqlTemplate = sqlTemplate.replace("$FEATURE_ELEMENT", lc.getFeatureElement());
            sqlTemplate = sqlTemplate.replace("$NAMESPACE_URI", lc.getFeatureNamespaceURI());
                sqlTemplate = sqlTemplate.replace("$WPS_PARAMS", wps_params);

            // write into file
            BufferedWriter writer = null;
            String filename = "";
            try {
                String filePath = "";
                if (PropertyUtil.get(WRITE_TEMPLATE_PATH) != null) {
                    filePath = PropertyUtil.get(WRITE_TEMPLATE_PATH);
                }
                else {
                    filePath = System.getProperty("java.io.tmpdir");
                }
                filename = filePath+lc.getLayerName().replace(":","_")+".sql";
                writer = new BufferedWriter(new FileWriter(filename));
                writer.write(sqlTemplate);

            } catch (IOException e) {
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                    info("WFS insert sql script written " + " -- sql file: " + filename);
                } catch (IOException e) {
                }
            }
            }
        }
    }
    public static void info(final Object ... args) {

        log.info(args);
        report.append("    " + log.getString(args) + "\n");

    }

    protected static String getTemplate(String templatePath)
            throws  IOException {
        String template = "";

        InputStream inp =  WFSServiceTester.class.getClassLoader().getResourceAsStream(templatePath);
        template = new Scanner(inp).useDelimiter("\\A").next();
        inp.close();

        return template;
    }

}

