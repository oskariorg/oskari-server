import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import groovy.util.logging.*

@Log4j
public class ktj_KiinteistorajanSijaintitiedot_Parser extends AbstractGroovyGMLParserRecipe.GML3 {

	def input_ns = "http://xml.nls.fi/ktjkiiwfs/2010/02";
    def output_ns = "http://xml.nls.fi/ktjkiiwfs/2010/02#";

    def PARSER = [
           
            "KiinteistorajanSijaintitiedot": { input_Feat ->

                def output_ID = O.KiinteistorajanSijaintitiedot.qn.unique();
                def output_props = properties();
                def output_geoms = geometries();

                input_Feat.readChildren().each { input_Feats ->

                    switch (input_Feats.qn) {

                        case I.KiinteistorajanSijaintitiedot.sijainti:
                          	input_Feats.readFirstChildGeometry(
                            	I.KiinteistorajanSijaintitiedot.geoms, output_geoms, O.Geom);
                            break;
                        default:
                            input_Feats.readPrimitive(I.KiinteistorajanSijaintitiedot.props, output_props,
                                    iri(output_ns, input_Feats.qn.getLocalPart()))
                            break;


                    }

                }

                output.vertex(output_ID, O.KiinteistorajanSijaintitiedot.qn,
                        output_props, EMPTY, output_geoms );

            }

    ];

    def I = [
            "KiinteistorajanSijaintitiedot": [
                    "qn": qn(input_ns, "KiinteistorajanSijaintitiedot"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_ns,
                            "paivityspvm",
                            "interpolointitapa"
                    ),
                    "sijainti" : qn(input_ns,"sijainti"),
                    "geoms": mapGeometryTypes("http://www.opengis.net/gml",
                            "Curve"
                    )

            ]
    ];

    /* Output */

    def O = [
            "Geom": iri("http://oskari.org/spatial#", "location"),
            "KiinteistorajanSijaintitiedot": [
                    "qn": iri(output_ns, "KiinteistorajanSijaintitiedot"),
                    "sijainti": iri(output_ns, "sijainti")
            ]
    ];


    public void parse() {


        output.prefix("_ns", output_ns);
        output.type(O.KiinteistorajanSijaintitiedot.qn, EMPTY,EMPTY,EMPTY);
        
        log.debug 'KiinteistorajanSijaintitiedot parser invoked'

        /* Process */
        def fcount = 0
        iter(input.root().descendantElementCursor(I.KiinteistorajanSijaintitiedot.qn)).each { input_Feat ->
            PARSER.KiinteistorajanSijaintitiedot(input_Feat);
            fcount++ ;
        }

		log.debug 'KiinteistorajanSijaintitiedot parser leaving after ' + fcount + ' features.'

    }


}