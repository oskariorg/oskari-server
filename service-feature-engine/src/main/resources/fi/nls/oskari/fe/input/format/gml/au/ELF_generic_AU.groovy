import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;


public class ELF_generic_AU_Parser extends AbstractGroovyGMLParserRecipe.GML32 {

    def input_au_ns = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";
    def input_ns = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
    def input_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0"
    def input_base_ns = "urn:x-inspire:specification:gmlas:BaseTypes:3.2";
    def input_gmd_ns = "http://www.isotc211.org/2005/gmd";
    def input_gml_ns = "http://www.opengis.net/gml/3.2";

    def output_ns = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0#";
    def output_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0#"

    def PARSER = [
        "AdministrativeUnit": { input_Feat ->

            def gmlid = input_Feat.attr(input_gml_ns, "id");
            def output_ID = O.AdministrativeUnit.qn.unique(gmlid);
            def output_props = properties();
            def output_geoms = geometries();

            input_Feat.readChildren().each { input_Feats ->

                switch (input_Feats.qn) {

                    case I.AdministrativeUnit.geometry:

                        input_Feats.readFirstChildGeometry(I.AdministrativeUnit.geoms, output_geoms,
                        O.Geom);
                        break;
                    case I.AdministrativeUnit.name:
                        input_Feats.readDescendants(I.GeographicalName.qn).each { featGNProps ->
                            PARSER.GeographicalName(featGNProps, output_ID, output_props, output_geoms);
                        }
                        break;
                    case I.AdministrativeUnit.inspireId:
                        input_Feats.readDescendants(I.AdministrativeUnit.Identifier).each { featIdentifierProps ->

                            featIdentifierProps.readChildren().each { featIdProps ->
                                featIdProps.readPrimitive(
                                        I.AdministrativeUnit.IdentifierProps, output_props,
                                        iri(output_ns, featIdProps.qn.getLocalPart())
                                        )
                            }
                        }
                        break;
                    case I.AdministrativeUnit.nationalLevelName:
                        input_Feats.readChildren().each { featGMDProps ->
                            featGMDProps.readPrimitive(
                                    I.AdministrativeUnit.gmdProps, output_props,
                                    iri(output_ns, "nationalLevelName")
                                    )
                        }
                        break;
                    case I.AdministrativeUnit.country:
                        input_Feats.readChildren().each { featGMDProps ->
                            featGMDProps.readPrimitive(
                                    I.AdministrativeUnit.gmdProps, output_props,
                                    iri(output_ns, "country")
                                    )
                        }
                        break;
                    case I.AdministrativeUnit.upperLevelUnit:
                        input_Feats.readXlink(output_props,
                        iri(output_ns, input_Feats.qn.getLocalPart())
                        )
                        break;
                    default:
                        input_Feats.readPrimitive(
                        I.AdministrativeUnit.props, output_props,
                        iri(output_ns, input_Feats.qn.getLocalPart())
                        )
                        break;
                }
            }

            output.vertex(output_ID, O.AdministrativeUnit.qn,
                    output_props, EMPTY, output_geoms);
        },
        "GeographicalName": { input_Feat, output_AdministrativeUnit_ID, output_props, output_geoms ->

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

            /*output.vertex(output_ID, O.GeographicalName.qn,
             output_props, EMPTY);*/
            /*output.edge(output_AdministrativeUnit_ID, O.AdministrativeUnit.name, output_ID);*/

        },
        "SpellingOfName" : { input_Feat, output_GeographicalName_ID, output_props, output_geoms ->

            def output_ID = O.SpellingOfName.qn.unique();
            //def output_props = properties();

            input_Feat.readChildren().each { input_Feats ->

                input_Feats.readPrimitive(I.SpellingOfName.props, output_props,
                        iri(output_gn_ns, input_Feats.qn.getLocalPart()));

            }

            /*output.vertex(output_ID, O.SpellingOfName.qn,
             output_props, output_geoms);*/
            /*output.edge(output_GeographicalName_ID, O.GeographicalName.spelling, output_ID);*/

        }
    ];

    def I = [
        "AdministrativeUnit": [
            "qn": qn(input_au_ns, "AdministrativeUnit"),
            "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_ns,
            "beginLifespanVersion",
            "endLifespanVersion",
            "localType",
            "nationalLevel"
            ),
            "gmdProps": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_gmd_ns,
            "Country",
            "LocalisedCharacterString"
            ),
            "geometry": qn(input_ns, "geometry"),
            "name": qn(input_ns, "name"),
            "geoms": mapGeometryTypes("http://www.opengis.net/gml/3.2",
            "MultiSurface"
            ),
            "country": qn(input_ns, "country"),
            "inspireId": qn(input_ns, "inspireId"),
            "nationalLevelName": qn(input_ns, "nationalLevelName"),
            "upperLevelUnit": qn(input_ns, "upperLevelUnit"),
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

    /* Output */

    def O = [
        "Geom": iri("http://oskari.org/spatial#", "location"),
        "AdministrativeUnit": [
            "qn": iri(output_ns, "AdministrativeUnit"),
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


    public void parse() {



        output.prefix("_ns", output_ns);
        output.prefix("_gn", output_gn_ns);

        output.type(O.AdministrativeUnit.qn,
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
                iri(output_ns, "beginLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "endLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "nationalLevel"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "nationalLevelName"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "country"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns, "name"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns, "sourceOfName"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns, "pronunciation"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns, "referenceName"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns, "text"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_gn_ns, "script"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "NUTS"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ns, "upperLevelUnit"),
                XSDDatatype.XSDstring
                )


                ),
                EMPTY,
                geometryTypes(
                pair(O.Geom, "GEOMETRY")));

        /* Process */
        def fcount = 0



        iter(input.root().descendantElementCursor(I.AdministrativeUnit.qn)).each { input_Feat ->
            PARSER.AdministrativeUnit(input_Feat);
            fcount++;
        }


    }


}