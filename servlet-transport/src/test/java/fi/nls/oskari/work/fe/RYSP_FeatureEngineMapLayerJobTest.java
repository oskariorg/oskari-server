package fi.nls.oskari.work.fe;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Layer;
import fi.nls.oskari.pojo.SessionStore;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertTrue;

public class RYSP_FeatureEngineMapLayerJobTest {
    protected static final Logger log = LogFactory
            .getLogger(RYSP_FeatureEngineMapLayerJobTest.class);

    private static final String permJSON = "{\"layerIds\":[34,32,12,8,17,28,36,15,10,26,11,38,4,18,30,16,33,6,19,29,2,21,3,23,31,35,20,5,13,22,9,7,37,14,27,1]}";

    private static final String sessionJSON = "{\"client\":\"jd1msc64ns1lbsj1j1r9pgpit0n7\","
            + "\"session\":\"08A31430DF0E847D6DE50A07E9FBB075\","
            + "\"route\":\".node1\",\"uuid\":\"\",\"language\":\"fi\",\"browser\":\"safari\",\"browserVersion\":537,"
            + "\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[237241.061,6709202.633,237818.061,6709549.633],\"zoom\":12},"
            + "\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":["
            + "[237216.0,6709504.0,237344.0,6709632.0],"
            + "[237344.0,6709504.0,237472.0,6709632.0],"
            + "[237472.0,6709504.0,237600.0,6709632.0],"
            + "[237600.0,6709504.0,237728.0,6709632.0],"
            + "[237728.0,6709504.0,237856.0,6709632.0],"
            + "[237856.0,6709504.0,237984.0,6709632.0],"
            + "[237216.0,6709376.0,237344.0,6709504.0],"
            + "[237344.0,6709376.0,237472.0,6709504.0],"
            + "[237472.0,6709376.0,237600.0,6709504.0],"
            + "[237600.0,6709376.0,237728.0,6709504.0],"
            + "[237728.0,6709376.0,237856.0,6709504.0],"
            + "[237856.0,6709376.0,237984.0,6709504.0],"
            + "[237216.0,6709248.0,237344.0,6709376.0],"
            + "[237344.0,6709248.0,237472.0,6709376.0],"
            + "[237472.0,6709248.0,237600.0,6709376.0],"
            + "[237600.0,6709248.0,237728.0,6709376.0],"
            + "[237728.0,6709248.0,237856.0,6709376.0],"
            + "[237856.0,6709248.0,237984.0,6709376.0],"
            + "[237216.0,6709120.0,237344.0,6709248.0],"
            + "[237344.0,6709120.0,237472.0,6709248.0],"
            + "[237472.0,6709120.0,237600.0,6709248.0],"
            + "[237600.0,6709120.0,237728.0,6709248.0],"
            + "[237728.0,6709120.0,237856.0,6709248.0],"
            + "[237856.0,6709120.0,237984.0,6709248.0]]},"
            + "\"tileSize\":{\"width\":256,\"height\":256},"
            + "\"mapSize\":{\"width\":1154,\"height\":694},"
            + "\"mapScales\":[5805342.72,2902671.36,1451335.68,725667.84,362833.92,181416.96,90708.48,45354.24,22677.12,11338.56,5669.28,2834.64,1417.32,708.66],"
            + "\"layers\":{\"4\":{\"id\":\"4\",\"styleName\":\"default\",\"visible\":true}}}";

    private static final String RYSPkantaLiikennevaylaWFSLayerJSON = "{\"selectedFeatureParams\":{},"
            + "\"jobType\":\"oskari-feature-engine\","
            + "\"getMapTiles\":true,"
            + "\"layerName\":\"RYSPkantaLiikennevayla\",\"featureElement\":\"Liikennevayla\",\"getHighlightImage\":true,"
            + "\"templateType\":\"mah taip\",\"GMLGeometryProperty\":\"geometry\",\"geometryNamespaceURI\":\"\","
            + "\"featureType\":{"
            + "\"default\":\""
            + "         *geometry:Geometry,name:String,beginLifespanVersion:String,endLifespanVersion:String"
            + "\"},"
            + "\"tileBuffer\":{\"default\":1,\"oskari_custom\":1},\"maxFeatures\":5000,\"maxScale\":1,"
            + "\"URL\":\"http://opaskartta.turku.fi/TeklaOGCWeb/WFS.ashx\","
            + "\"requestTemplate\":\"/fi/nls/oskari/fe/input/format/gml/krysp/kanta_Liikennevayla_wfs_template.xml\","
            + "\"isPublished\":false,\"featureParamsLocales\":{},"
            + "\"getFeatureInfo\":true,\"tileRequest\":false,"
            + "\"styles\":{\"2\":{\"SLDStyle\":\"/fi/nls/oskari/fe/output/style/inspire/gn/nls_fi.xml\","
            + "\"id\":\"2\","
            + "\"name\":\"oskari-feature-engine\"}},"
            + "\"layerId\":\"4\","
            + "\"WFSVersion\":\"2.0.0\","
            + "\"responseTemplate\":\"fi.nls.oskari.fi.rysp.recipe.kanta.RYSP_kanta_Liikennevayla_Parser\","
            + "\"GML2Separator\":false,"
            + "\"minScale\":50000,"
            + "\"featureNamespace\":\"kanta\","
            + "\"SRSName\":\"EPSG:3067\","
            + "\"GMLVersion\":\"3.1.1\","
            + "\"featureNamespaceURI\":\"http://www.paikkatietopalvelu.fi/gml/kantakartta\","
            + "\"templateDescription\":\"RYSP Liikennevayla PoC\","
            + "\"templateName\":\"RYSP Liikennevayla\",\"uiName\":\"RYSP Liikennevayla - Turku\",\"geometryType\":\"2d\"}";

    private static final String RYSPkantaRakennusWFSLayerJSON = "{\"selectedFeatureParams\":{},"
            + "\"jobType\":\"oskari-feature-engine\","
            + "\"getMapTiles\":true,"
            + "\"layerName\":\"RYSPkantaRakennus\",\"featureElement\":\"Rakennus\",\"getHighlightImage\":true,"
            + "\"templateType\":\"mah taip\",\"GMLGeometryProperty\":\"geometry\",\"geometryNamespaceURI\":\"\","
            + "\"featureType\":{"
            + "\"default\":\""
            + "         *geometry:Geometry,name:String,beginLifespanVersion:String,endLifespanVersion:String"
            + "\"},"
            + "\"tileBuffer\":{\"default\":1,\"oskari_custom\":1},\"maxFeatures\":5000,\"maxScale\":1,"
            + "\"URL\":\"http://opaskartta.turku.fi/TeklaOGCWeb/WFS.ashx\","
            + "\"requestTemplate\":\"/fi/nls/oskari/fe/input/format/gml/krysp/kanta_Rakennus_wfs_template.xml\","
            + "\"isPublished\":false,\"featureParamsLocales\":{},"
            + "\"getFeatureInfo\":true,\"tileRequest\":false,"
            + "\"styles\":{\"2\":{\"SLDStyle\":\"/fi/nls/oskari/fe/output/style/inspire/gn/nls_fi.xml\","
            + "\"id\":\"2\","
            + "\"name\":\"oskari-feature-engine\"}},"
            + "\"layerId\":\"4\","
            + "\"WFSVersion\":\"2.0.0\","
            + "\"responseTemplate\":\"fi.nls.oskari.fi.rysp.recipe.kanta.RYSP_kanta_Rakennus_Parser\","
            + "\"GML2Separator\":false,"
            + "\"minScale\":50000,"
            + "\"featureNamespace\":\"kanta\","
            + "\"SRSName\":\"EPSG:3067\","
            + "\"GMLVersion\":\"3.1.1\","
            + "\"featureNamespaceURI\":\"http://www.paikkatietopalvelu.fi/gml/kantakartta\","
            + "\"templateDescription\":\"RYSP Rakennus PoC\","
            + "\"templateName\":\"RYSP Rakennus\",\"uiName\":\"RYSP Rakennus - Turku\",\"geometryType\":\"2d\"}";

    protected static void setupProxy() {

        System.setProperty("http.proxyHost", "wwwp.nls.fi");
        System.setProperty("http.proxyPort", "800");
        System.setProperty("http.nonProxyHosts",
                "*.nls.fi|127.0.0.1|*.paikkatietoikkuna.fi");

    }

    @BeforeClass
    public static void setUp() throws IOException {
        setupProxy();

    }


    @org.junit.Ignore("Requires Backend")
    @Test
    public void testRYSPkantaLiikennevaylaMapLayerJob() throws IOException {

        CounterJsonResultProcessor resultProcessor = new JacksonCounterJsonResultProcessor();

        FEMapLayerJob job = TestHelper.createJob(sessionJSON, resultProcessor, RYSPkantaLiikennevaylaWFSLayerJSON);
        job.run();

        HashMap<String, Integer> results = resultProcessor.getResults();
        for (Entry<String, Integer> entry : results.entrySet()) {

            log.debug(entry.getKey(), entry.getValue());
        }

        assertTrue(results.get("/wfs/properties") != null
                && results.get("/wfs/properties") > 0);
        assertTrue(results.get("/wfs/feature") != null
                && results.get("/wfs/feature") > 0);
        assertTrue(results.get("/wfs/image") != null
                && results.get("/wfs/image") == 24);

    }

    @org.junit.Ignore("Requires Backend")
    @Test
    public void testRYSPkantaRakennusMapLayerJob() throws IOException {

        SessionStore session = SessionStore.setJSON(sessionJSON);

        Map<String, Layer> layers = session.getLayers();
        for (Layer layer : layers.values()) {
            layer.setTiles(session.getGrid().getBounds()); // init bounds to
                                                           // tiles (render
                                                           // all)
        }

        CounterJsonResultProcessor resultProcessor = new JacksonCounterJsonResultProcessor();

        FEMapLayerJob job = TestHelper.createJob(sessionJSON, resultProcessor, RYSPkantaRakennusWFSLayerJSON);
        job.run();

        HashMap<String, Integer> results = resultProcessor.getResults();
        for (Entry<String, Integer> entry : results.entrySet()) {

            log.debug(entry.getKey(), entry.getValue());
        }

        assertTrue(results.get("/wfs/properties") != null
                && results.get("/wfs/properties") > 0);
        assertTrue(results.get("/wfs/feature") != null
                && results.get("/wfs/feature") > 0);
        assertTrue(results.get("/wfs/image") != null
                && results.get("/wfs/image") == 24);

    }

}
