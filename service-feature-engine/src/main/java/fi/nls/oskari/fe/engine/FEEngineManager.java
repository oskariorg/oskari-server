package fi.nls.oskari.fe.engine;

import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FEEngineManager {

    protected static final Logger log = LogFactory
            .getLogger(FEEngineManager.class);
    /*
     * 
     * WFSLayerStore
     * 
     * "customParser" : "oskari-feature-engine", "requestTemplate" :
     * "/resource/path/to/request/template.xml", "responseTemplate" :
     * "/resource/path/to/response/groovy.groovy"1
     */

    static Map<String, Class<GroovyParserRecipe>> recipeClazzes = new ConcurrentHashMap<String, Class<GroovyParserRecipe>>();

    static GroovyClassLoader gcl = new GroovyClassLoader();

    public static FeatureEngine getEngineForRecipe(String recipePath)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        if (recipePath.endsWith(".groovy")) {
            return getGroovyEngineForRecipe(recipePath);
        } else {
            return getBasicEngineForRecipe(recipePath);
        }
    }

    private static FeatureEngine getBasicEngineForRecipe(String recipePath)
            throws IllegalAccessException, InstantiationException,
            ClassNotFoundException {

        BasicFeatureEngine engine = new BasicFeatureEngine();
        Class<ParserRecipe> recipeClazz = (Class<ParserRecipe>) Class
                .forName(recipePath);
        log.debug("[fe] Java recipe Lookup " + recipePath + " / " + recipeClazz);
        ParserRecipe instance = recipeClazz.newInstance();
        log.debug("[fe] Java recipe instance " + instance);
        engine.setRecipe(instance);
        return engine;
    }

    static FeatureEngine getGroovyEngineForRecipe(String recipePath)
            throws InstantiationException, IllegalAccessException {

        Class<GroovyParserRecipe> recipeClazz = recipeClazzes.get(recipePath);
        if (recipeClazzes.get(recipePath) == null) {

            synchronized (gcl) {
                try {
                    log.debug("[fe] Groovy recipe compiling " + recipePath
                            + " / " + recipePath);

                    InputStreamReader reader = new InputStreamReader(
                            FEEngineManager.class
                                    .getResourceAsStream(recipePath));

                    GroovyCodeSource codeSource = new GroovyCodeSource(reader,
                            recipePath, ".");

                    recipeClazz = (Class<GroovyParserRecipe>) gcl.parseClass(
                            codeSource, true);

                    recipeClazzes.put(recipePath, recipeClazz);

                    log.debug("[fe] Groovy caching recipe", recipePath);

                } catch (RuntimeException e) {

                    log.debug(e, "[fe] Groovy recipe setup FAILURE");

                } finally {

                    log.debug("[fe] Groovy recipe setup finalized");

                }
            }
        }

        GroovyFeatureEngine engine = new GroovyFeatureEngine();
        engine.setRecipe(recipeClazz.newInstance());

        return engine;

    }

}
