package fi.nls.oskari.work.fe;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.geotools.styling.Style;

import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.OWSMapLayerJob.Type;

public interface FESetup {

 
    GroovyParserRecipe getRecipe(String profile, OWSMapLayerJob.Type type)
            throws InstantiationException, IllegalAccessException;

    FERequestTemplate getRequestTemplate(String profile, Type type);

    Style getSLD(String profile, OWSMapLayerJob.Type type);

    FEUrl getTemplateUrl(String profile);

}
