package fi.nls.oskari.fe.input.format.gml;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.fe.engine.GroovyFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

/*
 * Test threaded Groovy script execution
 * Todo Test HttpClient threaded execution
 */
@Deprecated
public class TestGroovyFeatureEngine {

    static Map<String, Class<GroovyParserRecipe>> recipeClazzes = new ConcurrentHashMap<String, Class<GroovyParserRecipe>>();

    static GroovyClassLoader gcl = new GroovyClassLoader();

    static GroovyParserRecipe getRecipe(String recipePath)
            throws InstantiationException, IllegalAccessException {

        Class<GroovyParserRecipe> recipeClazz = recipeClazzes.get(recipePath);
        if (recipeClazzes.get(recipePath) == null) {

            synchronized (gcl) {
                try {

                    InputStreamReader reader = new InputStreamReader(
                            TestGroovyFeatureEngine.class
                                    .getResourceAsStream(recipePath));

                    GroovyCodeSource codeSource = new GroovyCodeSource(reader,
                            recipePath, ".");

                    recipeClazz = (Class<GroovyParserRecipe>) gcl.parseClass(
                            codeSource, true);

                    recipeClazzes.put(recipePath, recipeClazz);

                } catch (RuntimeException e) {

                    e.printStackTrace(System.err);

                } finally {

                }
            }
        }

        return recipeClazz.newInstance();

    }

    interface TestOutputProcessor extends OutputProcessor {

        public int getFeatureCount();
    }

    class WorkerRunnable implements Runnable {

        InputStream inp;
        GroovyParserRecipe recipe;
        int count = -1;

        public int getCount() {
            return count;
        }

        WorkerRunnable(InputStream inp, GroovyParserRecipe recipe) {
            this.inp = inp;
            this.recipe = recipe;
        }

        public void run() {
            System.out.println(Thread.currentThread().getName() + " Start.");

            processCommand();

            System.out.println(Thread.currentThread().getName() + " End.");

        }

        void processCommand() {

            GroovyFeatureEngine engine = new GroovyFeatureEngine();

            XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
            TestOutputProcessor outputProcessor = new TestOutputProcessor() {

                public static final String NS = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0#";

                int featureCount = 0;

                @Override
                public void begin() throws IOException {

                }

                @Override
                public void edge(Resource subject, Resource predicate,
                        Resource value) throws IOException {

                }

                @Override
                public void end() throws IOException {

                }

                @Override
                public void flush() throws IOException {

                }


                public void merge(List<JSONObject> list, Resource res) throws IOException {

                }

                public void equalizePropertyArraySize(Map<String,Integer> multiElemmap,  Map<String, Resource> resmap) {

                }

                @Override
                public void prefix(String prefix, String ns) throws IOException {

                }

                @Override
                public void type(Resource type,
                        List<Pair<Resource, XSDDatatype>> simpleProperties,
                        List<Pair<Resource, Object>> linkProperties,
                        List<Pair<Resource, String>> geometryProperties)
                        throws IOException {
                    assertTrue(type.getNs().equals(NS));

                }

                @Override
                public void vertex(Resource iri, Resource type,
                        List<Pair<Resource, Object>> simpleProperties,
                        List<Pair<Resource, Object>> linkProperties)
                        throws IOException {

                }

                @Override
                public void vertex(Resource iri, Resource type,
                        List<Pair<Resource, Object>> simpleProperties,
                        List<Pair<Resource, Object>> linkProperties,
                        List<Pair<Resource, Geometry>> geometryProperties)
                        throws IOException {

                    assertTrue(iri.getNs().equals(NS));
                    assertTrue(type.getNs().equals(NS));

                    assertTrue(!geometryProperties.isEmpty());
                    assertTrue(geometryProperties.get(0).getValue() != null);
                    assertTrue(geometryProperties.get(0).getValue() instanceof Point);

                    featureCount++;

                }

                @Override
                public int getFeatureCount() {
                    return featureCount;
                }

            };

            try {
                inputProcessor.setInput(inp);

                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XMLStreamException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    inp.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            count = outputProcessor.getFeatureCount();
        }
    }

    @Test
    public void test_threadedExecution() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        int nThreads = 20;
        int nJobs = 100;

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        WorkerRunnable[] jobs = new WorkerRunnable[nJobs];

        for (int i = 0; i < nJobs; i++) {

            InputStream inp = getClass()
                    .getResourceAsStream(
                            "/fi/nls/oskari/eu/elf/geographicalnames/geonorge_no-ELF-GN-wfs.xml");

            GroovyParserRecipe recipe = getRecipe("/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy");

            WorkerRunnable worker = new WorkerRunnable(inp, recipe);
            jobs[i] = worker;

            executor.execute(worker);

        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("Finished all threads");

        for (int i = 0; i < nJobs; i++) {
            System.out.println("#" + i + " -> " + jobs[i].getCount());
            assertTrue(jobs[i].getCount() == 65);
        }

    }
}
