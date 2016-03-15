package fi.nls.oskari.fe.input.format.gml.recipe;

import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fi.rysp.generic.WFS11_path_parse_worker;

/* Backwards compatibility for Groovy impls */
@Deprecated
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
    public void setParseWorker(ELF_path_parse_worker worker){


    }
    public void setWFS11ParseWorker(WFS11_path_parse_worker worker){


    }

}
