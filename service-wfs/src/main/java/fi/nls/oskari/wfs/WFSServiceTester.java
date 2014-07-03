package fi.nls.oskari.wfs;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Oskari team
 */
public class WFSServiceTester {

    private static Logger log = LogFactory.getLogger(WFSServiceTester.class);
    private final static String  KEY_LAYERS = "layers";
    private final static String  PROP_PROXYHOST = "http.proxyHost";
    private final static String  PROP_PROXYPORT = "http.proxyPort";
    private final static String  PROP_NONPROXYHOSTS = "http.nonproxyHosts";

    public static void main(String[] args) throws Exception {


        PropertyUtil.loadProperties("/service-wfs.properties");

        // replace logger after properties are populated
        log = LogFactory.getLogger(WFSServiceTester.class);

        // Get args
        if(args == null || args.length == 0)
        {
            log.info("incorrect parameters ");
            log.info("use - mvn  exec:java -Dexec.args='http://geo.stat.fi:8080/geoserver/wfs' ");
            System.exit(1);
        }

        // WFS service url e.g. http://geo.stat.fi:8080/geoserver/wfs
        final String url = args[0];
        final String version = "1.1.0";

        setProxy();

        log.info("Service url: "+url);

        // Test versions with pure GetCapabilities request
        TestWfsVersions(url);

        JSONObject featureTypes = GetGtWFSCapabilities.getWFSCapabilities(url, version);



        if(featureTypes == null) {
            log.info("GetCapabilities failed for service url: "+url);
            System.exit(1);
         }
        else log.info("GetCapabilities OK");

        // Test DescribeFeatureType
        TestDescribeFeatureTypes(featureTypes);

        // Test GetFeature

    }
    public static void TestDescribeFeatureTypes(JSONObject featureTypes) {

        // loop featureTypes
        JSONArray layers = featureTypes.optJSONArray(KEY_LAYERS);
        if (layers != null && layers.length() > 0 ) {
            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.optJSONObject(i);
                //
                String name = layer.optString("layerName");
            }
        }

    }

    /**
     * Test Wfs version support
     * @param serviceurl  Wfs service url
     */
    public static void TestWfsVersions(String serviceurl) {

        log.info("WFS version test:");
        String version = "1.0.0";
        GetGtWFSCapabilities.getUrl(serviceurl, version);
        log.info("GetCapabilities version "+version+" OK");

    }

    public static void setProxy() {

        Properties systemProperties = System.getProperties();
        if(PropertyUtil.get(PROP_PROXYHOST) != null ) systemProperties.setProperty(PROP_PROXYHOST,PropertyUtil.get(PROP_PROXYHOST));
        if(PropertyUtil.get(PROP_PROXYPORT) != null ) systemProperties.setProperty(PROP_PROXYPORT,PropertyUtil.get(PROP_PROXYPORT));
        if(PropertyUtil.get(PROP_NONPROXYHOSTS) != null ) systemProperties.setProperty(PROP_NONPROXYHOSTS,PropertyUtil.get(PROP_NONPROXYHOSTS));
    }




}
