import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import groovy.util.logging.*

@Commons
public class ELF_generic_TN_Parser extends AbstractGroovyGMLParserRecipe.GML32 {

    def input_ns = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0";
    def input_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
    def input_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2";
    def input_base_ns = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/";
    def input_gml_ns = "http://www.opengis.net/gml/3.2";

    def output_ns = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0#";
    def output_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";
    def output_tn_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";
    def output_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0#";


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

            // TODO properly support multiple languages
            output.vertex(/*output_ID*/O.RoadLink.qn.unique(), O.RoadLink.qn,
                    output_props, EMPTY, output_geoms);



        },
        "RoadLink": { input_Feat ->

            def gmlid = input_Feat.attr(input_gml_ns, "id");
            def output_ID = O.RoadLink.qn.unique(gmlid);
            def output_props = properties();
            def output_geoms = geometries();
            def placeNamesCount = 0;

            input_Feat.readChildren().each { input_Feats ->

                switch (input_Feats.qn) {

                    case I.RoadLink.geometry:
                        input_Feats.readFirstChildGeometry(I.RoadLink.geoms, output_geoms,
                        O.Geom);
                        break;
                    case I.RoadLink.inspireId:
                        input_Feats.readDescendants(I.RoadLink.Identifier).each { featIdentifierProps ->

                            featIdentifierProps.readChildren().each { featIdProps ->
                                featIdProps.readPrimitive(
                                        I.RoadLink.IdentifierProps, output_props,
                                        iri(output_ns, featIdProps.qn.getLocalPart())
                                        )
                            }
                        }
                        break;
                    case I.RoadLink.geographicalName:

                        input_Feats.readDescendants(I.GeographicalName.qn).each { featGNProps ->
                            PARSER.GeographicalName(featGNProps, output_ID, output_props, output_geoms);
                            placeNamesCount++;
                        }
                        break;

                    default:
                        input_Feats.readPrimitive(
                        I.RoadLink.props, output_props,
                        iri(output_tn_ns, input_Feats.qn.getLocalPart())
                        )
                        break;
                }
            }


            if( placeNamesCount == 0 ) {
                output.vertex(output_ID, O.RoadLink.qn,
                        output_props, EMPTY, output_geoms);
            }


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
        "RoadLink": [
            "qn": qn(input_ns, "RoadLink"),
            "props": mapPrimitiveTypes(
            XSDDatatype.XSDstring,
            input_net_ns,
            "beginLifespanVersion",
            "endLifespanVersion",
            "localType"
            ),
            "geometry": qn(input_net_ns, "centrelineGeometry"),
            "geographicalName": qn(input_net_ns, "geographicalName"),
            "geoms": mapGeometryTypes("http://www.opengis.net/gml/3.2",
            "LineString", "Curve", "CompositeCurve", "OrientableCurve","MultiCurve"
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
            "qn": qn(input_gn_ns, "GeographicalName"),
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
            "qn": qn(input_gn_ns, "SpellingOfName"),
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
        "RoadLink": [
            "qn": iri(output_ns, "RoadLink"),
            "geometry": iri(output_ns, "geometry"),
            "geographicalName": iri(output_tn_ns, "geographicalName")
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
        output.prefix("_tn", output_tn_ns);

        /* Declare a Type for Transport */
        /* Todo To be based on Transport specs given to this class */
        output.type(O.RoadLink.qn,
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
                iri(output_tn_ns,"beginLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_tn_ns,"endLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_tn_ns,"localType"),
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

        iter(input.root().descendantElementCursor(I.RoadLink.qn)).each { input_Feat ->
            PARSER.RoadLink(input_Feat);
            fcount++;

            //System.out.println(input_Feat);
        }


    }


}