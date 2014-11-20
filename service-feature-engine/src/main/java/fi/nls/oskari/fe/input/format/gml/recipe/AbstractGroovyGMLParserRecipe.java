package fi.nls.oskari.fe.input.format.gml.recipe;

import fi.nls.oskari.fe.input.format.gml.FEPullParser;

/* Backwards compatibility for Groovy impls */
public abstract class AbstractGroovyGMLParserRecipe extends
        StaxMateGMLParserRecipeBase implements GroovyParserRecipe {

    public static abstract class GML2 extends AbstractGroovyGMLParserRecipe {
        {
            gml = new org.geotools.gml2.GMLConfiguration();
            parserAny = new FEPullParser(gml, null);
        }

    }

    public static abstract class GML3 extends AbstractGroovyGMLParserRecipe {
        {
            gml = new GML31_Configuration();
            parserAny = new FEPullParser(gml, null);

        }

    }

    public static abstract class GML32 extends AbstractGroovyGMLParserRecipe {
        {
            gml = new org.geotools.gml3.v3_2.GMLConfiguration(true);
            parserAny = new FEPullParser(gml, null);
        }

    }

}
