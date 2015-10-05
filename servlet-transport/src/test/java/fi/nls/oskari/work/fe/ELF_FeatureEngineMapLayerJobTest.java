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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

public class ELF_FeatureEngineMapLayerJobTest {
    protected static final Logger log = LogFactory
            .getLogger(ELF_FeatureEngineMapLayerJobTest.class);

    private static final String permJSON = "{\"layerIds\":[34,32,12,8,17,28,36,15,10,26,11,38,4,18,30,16,33,6,19,29,2,21,3,23,31,35,20,5,13,22,9,7,37,14,27,1]}";

    private static final String sessionJSON = "{\"client\":\"d1mkrsnwpwuj6310e8opd9erzmu\","
            + "\"session\":\"15qirincz105v1dopee0nsigit\","
            + "\"route\":\"\",\"uuid\":\"\","
            + "\"language\":\"en\",\"browser\":\"safari\","
            + "\"browserVersion\":537,"
            + "\"location\":{\"srs\":\"EPSG:3857\","
            + "\"bbox\":[2754620.0241455,8417996.6562588,2799641.4337993,8450291.3007048],\"zoom\":9},"
            + "\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":["
            + "[2749287.0329785,8443539.8913184,2759070.9725977,8453323.8309375],"
            + "[2759070.9725977,8443539.8913184,2768854.9122168,8453323.8309375],"
            + "[2768854.9122168,8443539.8913184,2778638.8518359,8453323.8309375],"
            + "[2778638.8518359,8443539.8913184,2788422.7914551,8453323.8309375],"
            + "[2788422.7914551,8443539.8913184,2798206.7310742,8453323.8309375],"
            + "[2798206.7310742,8443539.8913184,2807990.6706934,8453323.8309375],"
            + "[2749287.0329785,8433755.9516992,2759070.9725977,8443539.8913184],"
            + "[2759070.9725977,8433755.9516992,2768854.9122168,8443539.8913184],"
            + "[2768854.9122168,8433755.9516992,2778638.8518359,8443539.8913184],"
            + "[2778638.8518359,8433755.9516992,2788422.7914551,8443539.8913184],"
            + "[2788422.7914551,8433755.9516992,2798206.7310742,8443539.8913184],"
            + "[2798206.7310742,8433755.9516992,2807990.6706934,8443539.8913184],"
            + "[2749287.0329785,8423972.0120801,2759070.9725977,8433755.9516992],"
            + "[2759070.9725977,8423972.0120801,2768854.9122168,8433755.9516992],"
            + "[2768854.9122168,8423972.0120801,2778638.8518359,8433755.9516992],"
            + "[2778638.8518359,8423972.0120801,2788422.7914551,8433755.9516992],"
            + "[2788422.7914551,8423972.0120801,2798206.7310742,8433755.9516992],"
            + "[2798206.7310742,8423972.0120801,2807990.6706934,8433755.9516992],"
            + "[2749287.0329785,8414188.0724609,2759070.9725977,8423972.0120801],"
            + "[2759070.9725977,8414188.0724609,2768854.9122168,8423972.0120801],"
            + "[2768854.9122168,8414188.0724609,2778638.8518359,8423972.0120801],"
            + "[2778638.8518359,8414188.0724609,2788422.7914551,8423972.0120801],"
            + "[2788422.7914551,8414188.0724609,2798206.7310742,8423972.0120801],"
            + "[2798206.7310742,8414188.0724609,2807990.6706934,8423972.0120801],"
            + "[2749287.0329785,8404404.1328418,2759070.9725977,8414188.0724609],"
            + "[2759070.9725977,8404404.1328418,2768854.9122168,8414188.0724609],"
            + "[2768854.9122168,8404404.1328418,2778638.8518359,8414188.0724609],"
            + "[2778638.8518359,8404404.1328418,2788422.7914551,8414188.0724609],"
            + "[2788422.7914551,8404404.1328418,2798206.7310742,8414188.0724609],"
            + "[2798206.7310742,8404404.1328418,2807990.6706934,8414188.0724609]"
            + "]},"
            + "\"tileSize\":{\"width\":256,\"height\":256},"
            + "\"mapSize\":{\"width\":1178,\"height\":845},"
            + "\"mapScales\":[5.546789320400156E7,2.773394660200078E7,1.386697330100039E7,6933486.6505002,3466743.3252501,1733371.66262505,866685.83131252,433342.91565626,216671.45782813,108335.72891407,54167.86445703,27083.93222852,13541.96611426,6770.98305713,3385.49152856,1692.74576428,846.37288214,423.18644107,211.59322054],"
            + "\"layers\":{\"4\":{\"id\":\"4\",\"styleName\":\"default\",\"visible\":true}}}";

    private static final String ELFNamedPlaceGroovyLayerJSON = "{\"selectedFeatureParams\":{},"
            + "\"jobType\":\"oskari-feature-engine\","
            + "\"getMapTiles\":true,\"layerName\":\"ELF_GN_nls_fi\","
            + "\"featureElement\":\"NamedPlace\",\"getHighlightImage\":true,\"templateType\":\"mah taip\","
            + "\"GMLGeometryProperty\":\"geometry\",\"geometryNamespaceURI\":\"\","
            + "\"featureType\":{"
            + "\"default\":\"*geometry:Geometry,text:String,script:String,sourceOfName:String,nameStatus:String,nativeness:String,language:String,beginLifespanVersion:String,endLifespanVersion:String,localType:String"
            + "\"},"
            + "\"tileBuffer\":{\"default\":1,\"oskari_custom\":1},\"maxFeatures\":5000,\"maxScale\":1,"
            + "\"URL\":\"!http://visukarttake01.nls.fi:8080/elf-wfs/services/elf-lod1gn|http://195.156.69.59/elf-wfs/services/elf-lod1gn\","
            + "\"requestTemplate\":\"/fi/nls/oskari/fe/input/request/wfs/gn/ELF_generic_GN_wfs_template.xml\","
            + "\"isPublished\":false,"
            + "\"featureParamsLocales\":{},\"getFeatureInfo\":true,"
            + "\"tileRequest\":false,"
            + "\"styles\":{\"2\":{\"SLDStyle\":\"/fi/nls/oskari/fe/output/style/inspire/gn/nls_fi.xml\",\"id\":\"2\","
            + "\"name\":\"oskari-feature-engine\"}},\"layerId\":\"4\",\"WFSVersion\":\"2.0.0\","
            + "\"responseTemplate\":\"/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy\",\"GML2Separator\":false,\"minScale\":120000,"
            + "\"featureNamespace\":\"elf-lod1gn\","
            + "\"SRSName\":\"EPSG:3857\","
            + "\"GMLVersion\":\"3.2.1\","
            + "\"featureNamespaceURI\":\"http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0\","
            + "\"templateDescription\":\"ELF GN PoC\",\"templateName\":\"ELF GN\",\"uiName\":\"GN Geographical Names - nls.fi\",\"geometryType\":\"2d\"}";

    private static final String ELFNamedPlaceJavaLayerJSON = "{\"selectedFeatureParams\":{},"
            + "\"jobType\":\"oskari-feature-engine\","
            + "\"getMapTiles\":true,"
            + "\"layerName\":\"ELF_GN_nls_fi\",\"featureElement\":\"NamedPlace\",\"getHighlightImage\":true,"
            + "\"templateType\":\"mah taip\",\"GMLGeometryProperty\":\"geometry\",\"geometryNamespaceURI\":\"\","
            + "\"featureType\":{"
            + "\"default\":\"*geometry:Geometry,"
            + "         text:String,script:String,sourceOfName:String,nameStatus:String,nativeness:String,language:String,beginLifespanVersion:String,endLifespanVersion:String,localType:String"
            + "\"},"
            + "\"tileBuffer\":{\"default\":1,\"oskari_custom\":1},\"maxFeatures\":5000,\"maxScale\":1,"
            + "\"URL\":\"!http://visukarttake01.nls.fi:8080/elf-wfs/services/elf-lod1gn|http://195.156.69.59/elf-wfs/services/elf-lod1gn\","
            + "\"requestTemplate\":\"/fi/nls/oskari/fe/input/request/wfs/gn/ELF_generic_GN_wfs_template.xml\","
            + "\"isPublished\":false,\"featureParamsLocales\":{},"
            + "\"getFeatureInfo\":true,\"tileRequest\":false,"
            + "\"styles\":{\"2\":{\"SLDStyle\":\"/fi/nls/oskari/fe/output/style/inspire/gn/nls_fi.xml\",\"id\":\"2\",\"name\":\"oskari-feature-engine\"}},\"layerId\":\"4\",\"WFSVersion\":\"2.0.0\","
            + "\"responseTemplate\":\"fi.nls.oskari.eu.elf.recipe.geographicalnames.ELF_MasterLoD1_NamedPlace_Parser\","
            + "\"GML2Separator\":false,\"minScale\":120000,"
            + "\"featureNamespace\":\"elf-lod1gn\","
            + "\"SRSName\":\"EPSG:3857\","
            + "\"GMLVersion\":\"3.2.1\","
            + "\"featureNamespaceURI\":\"http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0\","
            + "\"templateDescription\":\"ELF GN PoC\","
            + "\"templateName\":\"ELF GN\",\"uiName\":\"GN Geographical Names - nls.fi\",\"geometryType\":\"2d\"}";

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
    public void testNamedPlaceMapLayerJob() throws IOException {

        SessionStore session = SessionStore.setJSON(sessionJSON);

        Map<String, Layer> layers = session.getLayers();
        for (Layer layer : layers.values()) {
            layer.setTiles(session.getGrid().getBounds()); // init bounds to
                                                           // tiles (render
                                                           // all)
        }

        CounterJsonResultProcessor resultProcessor = new JacksonCounterJsonResultProcessor();

        FEMapLayerJob job = TestHelper.createJob(sessionJSON, resultProcessor, ELFNamedPlaceJavaLayerJSON);
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
                && results.get("/wfs/image") == 30);

    }

    @org.junit.Ignore("Requires Backend")
    @Test
    public void testNamedPlaceMapLayerJobtWithJettyJson() throws IOException {


        CounterJsonResultProcessor resultProcessor = new JettyCounterJsonResultProcessor();

        final FEMapLayerJob job = TestHelper.createJob(sessionJSON, resultProcessor, ELFNamedPlaceJavaLayerJSON);
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
                && results.get("/wfs/image") == 30);

    }

    @org.junit.Ignore("Requires Backend")
    @Test
    public void testConcurrentRequests() throws IOException {
        CounterJsonResultProcessor resultProcessor = new JacksonCounterJsonResultProcessor();

        int nThreads = 5;
        int nJobs = 10;

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        FEMapLayerJob[] jobs = new FEMapLayerJob[nJobs];

        for (int i = 0; i < nJobs; i++) {
            final FEMapLayerJob job = TestHelper.createJob(sessionJSON, resultProcessor, ELFNamedPlaceGroovyLayerJSON);
            jobs[i] = job;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    job.run();
                }
            });

        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
                log.debug("Waiting for Jobs to finish");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.debug("Finished all threads");

    }

}
