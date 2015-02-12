package fi.nls.oskari.work.fe;

import java.io.IOException;

import org.junit.BeforeClass;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class FEMapLayerJobTest {
    protected static final Logger log = LogFactory
            .getLogger(FEMapLayerJobTest.class);


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
    public void testNamedPlaceGroovyRequest() throws IOException {

        SessionStore session = SessionStore.setJSON(sessionJSON);

        Map<String, Layer> layers = session.getLayers();
        for (Layer layer : layers.values()) {
            layer.setTiles(session.getGrid().getBounds()); // init bounds to
                                                           // tiles (render
                                                           // all)
        }

        TestResultProcessor resultProcessor = new TestResultProcessor();

        TestRunFEMapLayerJob job = new TestRunFEMapLayerJob(resultProcessor,
                session, FEMapLayerJobTest.groovyLayerJSON);

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
    public void testNamedPlaceJavaRequest() throws IOException {

        SessionStore session = SessionStore.setJSON(sessionJSON);

        Map<String, Layer> layers = session.getLayers();
        for (Layer layer : layers.values()) {
            layer.setTiles(session.getGrid().getBounds()); // init bounds to
                                                           // tiles (render
                                                           // all)
        }

        TestResultProcessor resultProcessor = new TestResultProcessor();

        TestRunFEMapLayerJob job = new TestRunFEMapLayerJob(resultProcessor,
                session, FEMapLayerJobTest.javaLayerJSON);

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

        SessionStore session = SessionStore.setJSON(sessionJSON);

        Map<String, Layer> layers = session.getLayers();
        for (Layer layer : layers.values()) {
            layer.setTiles(session.getGrid().getBounds()); // init bounds to
                                                           // tiles (render
                                                           // all)
        }

        TestResultProcessor resultProcessor = new TestResultProcessor();

        int nThreads = 5;
        int nJobs = 10;

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        TestRunFEMapLayerJob[] jobs = new TestRunFEMapLayerJob[nJobs];

        for (int i = 0; i < nJobs; i++) {
            TestRunFEMapLayerJob job = new TestRunFEMapLayerJob(
                    resultProcessor, session, FEMapLayerJobTest.groovyLayerJSON);
            jobs[i] = job;
            executor.execute(job);

        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.currentThread().sleep(1000);
                log.debug("Waiting for Jobs to finish");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.debug("Finished all threads");

    }

    class TestResultProcessor implements ResultProcessor {

        long resultsCounter = 0;
        final HashMap<String, Integer> results = new HashMap<String, Integer>();
        
        
        public HashMap<String, Integer> getResults() {
            return results;
        }

        @Override
        public void addResults(String clientId, String channel, Object data) {
            // TODO Auto-generated method stub
            
            // display a snapshot
            if( resultsCounter < 10 ) {
                                
                ByteArrayOutputStream outs = new ByteArrayOutputStream();
                try {
                    writer.writeValue(outs, data);
                } catch (JsonGenerationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                log.debug(new String(outs.toByteArray()));
            }
            resultsCounter++ ;
            
            
            
            // log.debug(clientId, channel, data);
            if (results.get(channel) == null) {
                results.put(channel, 1);
            } else {
                results.put(channel, results.get(channel) + 1);
            }
        }

    };

    /* Test Helper to setup session and permission */
    class TestRunFEMapLayerJob extends FEMapLayerJob {

        private String layerjson;

        TestRunFEMapLayerJob(ResultProcessor resultProcessor,
                SessionStore session, String layerjson) {
            super(resultProcessor, Type.NORMAL, session, "4", true, true, true);
            this.layerjson = layerjson;
        }

        @Override
        protected boolean hasPermissionsForJob() {
            return true;
        }

        @Override
        protected WFSLayerStore getLayerForJob() {
            // TODO Auto-generated method stub
            try {
                return WFSLayerStore.setJSON(layerjson);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}
