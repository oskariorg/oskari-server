import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import groovy.util.logging.*

@Commons
public class ELF_generic_GN_Parser extends AbstractGroovyGMLParserRecipe.GML32 {

    def input_ns = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0";
    def input_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0"
    def input_base_ns = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/"
    def input_gml_ns = "http://www.opengis.net/gml/3.2";

    def output_ns = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0#";
    def output_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0#"


    // PARSER rules for elements
    def PARSER = [
        "GeographicalName": { input_Feat, output_NamedPlace_ID, output_props, output_geoms ->

            def output_ID = O.GeographicalName.qn.unique();
            //def output_props = properties();

            input_Feat.readChildren().each { input_Feats ->

                switch (input_Feats.qn) {
                    case I.GeographicalName.spelling:
                        input_Feats.readDescendants(I.SpellingOfName.qn).each { featGNProps ->
                            PARSER.SpellingOfName(featGNProps, output_ID, output_props, output_geoms);
                        }
                        break;
                    default:
                        input_Feats.readPrimitive(I.GeographicalName.props, output_props,
                        iri(output_gn_ns, input_Feats.qn.getLocalPart()));

                }
            }

            output.vertex(/*output_ID*/O.NamedPlace.qn.unique(), O.NamedPlace.qn,
                    output_props, EMPTY, output_geoms);


        },
        "NamedPlace": { input_Feat ->

            def gmlid = input_Feat.attr(input_gml_ns, "id");
            def output_ID = O.NamedPlace.qn.unique(gmlid);
            def output_props = properties();
            def output_geoms = geometries();
            def placeNamesCount = 0;

            input_Feat.readChildren().each { input_Feats ->

                switch (input_Feats.qn) {

                    case I.NamedPlace.geometry:
                        input_Feats.readFirstChildGeometry(I.NamedPlace.geoms, output_geoms,
                        O.Geom);
                        break;
                    case I.NamedPlace.inspireId:
                        input_Feats.readDescendants(I.NamedPlace.Identifier).each { featIdentifierProps ->

                            featIdentifierProps.readChildren().each { featIdProps ->
                                featIdProps.readPrimitive(
                                        I.NamedPlace.IdentifierProps, output_props,
                                        iri(output_ns, featIdProps.qn.getLocalPart())
                                        )
                            }
                        }
                        break;
                    case I.NamedPlace.name:

                        input_Feats.readDescendants(I.GeographicalName.qn).each { featGNProps ->
                            PARSER.GeographicalName(featGNProps, output_ID, output_props, output_geoms);
                            placeNamesCount++;
                        }
                        break;
                    default:
                        input_Feats.readPrimitive(
                        I.NamedPlace.props, output_props,
                        iri(output_gn_ns, input_Feats.qn.getLocalPart())
                        )
                        break;
                }
            }

            if( placeNamesCount == 0 ) {
                output.vertex(output_ID, O.NamedPlace.qn,
                        output_props, EMPTY, output_geoms);
            }
            /*output.vertex(output_ID, O.NamedPlace.qn,
             output_props, EMPTY, output_geoms);*/

        },
        "SpellingOfName" : { input_Feat, output_GeographicalName_ID, output_props, output_geoms ->

            def output_ID = O.SpellingOfName.qn.unique();
            //def output_props = properties();

            input_Feat.readChildren().each { input_Feats ->

                input_Feats.readPrimitive(I.SpellingOfName.props, output_props,
                        iri(output_gn_ns, input_Feats.qn.getLocalPart()));

            }

            output.vertex(output_ID, O.SpellingOfName.qn,
                    output_props, output_geoms);
            /*output.edge(output_GeographicalName_ID, O.GeographicalName.spelling, output_ID);*/

        }
    ];

    // QNames for INPUT elements
    def I = [
        "NamedPlace": [
            "qn": qn(input_ns, "NamedPlace"),
            "props": mapPrimitiveTypes(
            XSDDatatype.XSDstring,
            input_gn_ns,
            "beginLifespanVersion",
            "endLifespanVersion",
            "localType"
            ),
            "geometry": qn(input_gn_ns, "geometry"),
            "name": qn(input_gn_ns, "name"),
            "geoms": mapGeometryTypes("http://www.opengis.net/gml/3.2",
            "Point","MultiPoint"
            ),
            "inspireId": qn(input_ns, "inspireId"),
            "Identifier": qn(input_base_ns, "Identifier"),
            "IdentifierProps": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_base_ns,
            "localId",
            "namespace",
            "versionId"
            )

        ],
        "GeographicalName": [
            "qn": qn(input_ns, "GeographicalName"),
            "spelling": qn(input_gn_ns, "spelling"),
            "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_gn_ns,
            "language",
            "sourceOfName",
            "pronunciation",
            "referenceName"
            )
        ],
        "SpellingOfName": [
            "qn": qn(input_ns, "SpellingOfName"),
            "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_gn_ns,
            "text",
            "script"
            )
        ]
    ];

    /* QNames for Output Elements */
    def O = [
        "Geom": iri("http://oskari.org/spatial#", "location"),
        "NamedPlace": [
            "qn": iri(output_ns, "NamedPlace"),
            "geometry": iri(output_ns, "geometry"),
            "name": iri(output_gn_ns, "name")
        ],
        "GeographicalName": [
            "qn": iri(output_ns, "GeographicalName"),
            "spelling": iri(output_gn_ns, "spelling")
        ],
        "SpellingOfName": [
            "qn": iri(output_ns, "SpellingOfName"),
            "text": iri(output_gn_ns, "text")
        ]
    ];


    /* Entry point */
    public void parse() {

        /* Declare prfixes (mainly for JSON-LD to be more compact) */
        output.prefix("_ns", output_ns);
        output.prefix("_gn", output_gn_ns);

        /* Declare a Type for Transport */
        /* Todo To be based on Transport specs given to this class */
        output.type(O.NamedPlace.qn,
                simpleTypes(
                pair(
                iri(output_ns, "localId"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "namespace"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "versionId"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"beginLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"endLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"localType"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"language"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"sourceOfName"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"pronunciation"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"referenceName"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"text"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns,"script"),
                XSDDatatype.XSDstring
                )

                ),
                EMPTY,
                geometryTypes(
                pair(O.Geom, "GEOMETRY" ) ) );


        /* Process */
        def fcount = 0

        iter(input.root().descendantElementCursor(I.NamedPlace.qn)).each { input_Feat ->
            PARSER.NamedPlace(input_Feat);
            fcount++;

            //System.out.println(input_Feat);
        }


    }


}