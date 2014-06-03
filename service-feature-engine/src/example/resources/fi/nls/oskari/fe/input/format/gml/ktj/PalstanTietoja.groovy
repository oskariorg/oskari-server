import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import groovy.util.logging.*

@Log4j
public class ktj_PalstanTietoja_Parser extends AbstractGroovyGMLParserRecipe.GML3 {

	def input_ns = "http://xml.nls.fi/ktjkiiwfs/2010/02";
    def output_ns = "http://xml.nls.fi/ktjkiiwfs/2010/02#";

    def PARSER = [
           
            "KiinteistorajanTietoja": { input_Feat, p_ID ->

                def output_ID = O.KiinteistorajanTietoja.qn.unique();
                def output_props = properties();
                def output_geoms = geometries();

                input_Feat.readChildren().each { input_Feats ->

                    switch (input_Feats.qn) {

                        case I.KiinteistorajanTietoja.sijainti:
                          	input_Feats.readFirstChildGeometry(
                            	I.KiinteistorajanTietoja.geoms, output_geoms, O.Geom);
                            break;
                        default:
                            input_Feats.readPrimitive(I.KiinteistorajanTietoja.props, output_props,
                                    iri(output_ns, input_Feats.qn.getLocalPart()))
                            break;


                    }

                }

                output.vertex(output_ID, O.KiinteistorajanTietoja.qn,
                        output_props, EMPTY, output_geoms );

            },
             "RajamerkinTietoja": { input_Feat, p_ID ->

                def output_ID = O.RajamerkinTietoja.qn.unique();
                def output_props = properties();
                def output_geoms = geometries();

                input_Feat.readChildren().each { input_Feats ->

                    switch (input_Feats.qn) {

                        case I.RajamerkinTietoja.sijainti:
                          	input_Feats.readFirstChildGeometry(
                            	I.RajamerkinTietoja.geoms, output_geoms, O.Geom);
                            break;
                        default:
                            input_Feats.readPrimitive(I.RajamerkinTietoja.props, output_props,
                                    iri(output_ns, input_Feats.qn.getLocalPart()))
                            break;


                    }

                }

                output.vertex(output_ID, O.RajamerkinTietoja.qn,
                        output_props, EMPTY, output_geoms );

            },                     
            "PalstanTietoja": { input_Feat ->

                def output_ID = O.PalstanTietoja.qn.unique();
                def output_props = properties();
                def output_geoms = geometries();

                input_Feat.readChildren().each { input_Feats ->

                    switch (input_Feats.qn) {

                        case I.PalstanTietoja.sijainti:
                          	input_Feats.readFirstChildGeometry(
                            	I.PalstanTietoja.geoms, output_geoms, O.Geom);
                            break;
                        case I.PalstanTietoja.kiinteistorajanTietoja:
                        	input_Feats.readDescendants(I.KiinteistorajanTietoja.qn).each { featGNProps ->
                                PARSER.KiinteistorajanTietoja(featGNProps, output_ID);
                            }
                        	break;
                        case I.PalstanTietoja.rajamerkinTietoja:
                        	input_Feats.readDescendants(I.RajamerkinTietoja.qn).each { featGNProps ->
                                PARSER.RajamerkinTietoja(featGNProps, output_ID);
                            }
                        	break;
                        	
                        default:
                            input_Feats.readPrimitive(I.PalstanTietoja.props, output_props,
                                    iri(output_ns, input_Feats.qn.getLocalPart()))
                            break;


                    }

                }

                output.vertex(output_ID, O.PalstanTietoja.qn,
                        output_props, EMPTY, output_geoms );

            }

    ];

    def I = [
            "KiinteistorajanTietoja": [
                    "qn": qn(input_ns, "KiinteistorajanTietoja"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_ns,
                            "paivityspvm",
                            "interpolointitapa"
                    ),
                    "sijainti" : qn(input_ns,"sijainti"),
                    "geoms": mapGeometryTypes("http://www.opengis.net/gml",
                            "Curve"
                    )
            ],
            "RajamerkinTietoja": [
                    "qn": qn(input_ns, "RajamerkinTietoja"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_ns,
                            "paivityspvm",
                            "rajamerkkilaji"
                    ),
                    "sijainti" : qn(input_ns,"sijainti"),
                    "geoms": mapGeometryTypes("http://www.opengis.net/gml",
                            "Point"
                    )
            ],
             "PalstanTietoja": [
                    "qn": qn(input_ns, "PalstanTietoja"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_ns,
                            "paivityspvm",
                            "tekstiKartalla",
                            "rekisteriyksikonKiinteistotunnus"
                    ),
                    "sijainti" : qn(input_ns,"sijainti"),
                    "kiinteistorajanTietoja" : qn(input_ns,"kiinteistorajanTietoja"),
                    "rajamerkinTietoja" : qn(input_ns,"rajamerkinTietoja"),
                    "geoms": mapGeometryTypes("http://www.opengis.net/gml",
                            "Surface"
                    )
            ]
    ];

    /* Output */

    def O = [
            "Geom": iri("http://oskari.org/spatial#", "location"),
            "PalstanTietoja": [
                    "qn": iri(output_ns, "PalstanTietoja"),
                    "sijainti": iri(output_ns, "sijainti")
            ],
             "KiinteistorajanTietoja": [
                    "qn": iri(output_ns, "KiinteistorajanTietoja"),
                    "sijainti": iri(output_ns, "sijainti")
            ],
             "RajamerkinTietoja": [
                    "qn": iri(output_ns, "KiinteistorajanTietoja"),
                    "sijainti": iri(output_ns, "sijainti")
            ]
            
    ];


    public void parse() {


        output.prefix("_ns", output_ns);
        output.type(O.PalstanTietoja.qn, 
        	simpleTypes(
				pair(
					iri(output_ns,"paivityspvm"),
					XSDDatatype.XSDstring
				),
				pair(
					iri(output_ns,"tekstiKartalla"),
					XSDDatatype.XSDstring
				),
				pair(
					iri(output_ns,"rekisteriyksikonKiinteistotunnus"),
					XSDDatatype.XSDstring
				)
			),
			EMPTY,
			geometryTypes(
				pair(O.Geom, "GEOMETRY" )
			)
		);
        
        log.debug 'PalstanTietoja parser invoked'

        /* Process */
        def fcount = 0
        iter(input.root().descendantElementCursor(I.PalstanTietoja.qn)).each { input_Feat ->
            PARSER.PalstanTietoja(input_Feat);
            fcount++ ;
        }

		log.debug 'PalstanTietoja parser leaving after ' + fcount + ' features.'

    }


}