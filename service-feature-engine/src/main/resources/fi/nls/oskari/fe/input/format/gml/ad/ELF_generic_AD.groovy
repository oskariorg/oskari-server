import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class ELF_generic_ad_Parser extends AbstractGroovyGMLParserRecipe.GML32 {

    def input_ns = "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0";
    def input_ad_ns = "urn:x-inspire:specification:gmlas:Addresses:3.0"
    def input_base_ns = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/"
    def input_gml_ns = "http://www.opengis.net/gml/3.2";

    def output_ns = "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0#";
    def output_ad_ns = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3#";

    def PARSER = [
        "Address": { input_Feat ->

            def gmlid = input_Feat.attr(input_gml_ns, "id");
            def output_ID = O.Address.qn.unique(gmlid);
            def output_props = properties();
            def output_geoms = geometries();

            input_Feat.readChildren().each { input_Feats ->

                switch (input_Feats.qn) {
                    case I.Address.inspireId:
                        input_Feats.readDescendants(I.Address.Identifier).each { featIdentifierProps ->

                            featIdentifierProps.readChildren().each { featIdProps ->
                                featIdProps.readPrimitive(
                                        I.Address.IdentifierProps, output_props,
                                        iri(output_ns, featIdProps.qn.getLocalPart())
                                        )
                            }
                        }
                        break;
                    case I.Address.position:
                        input_Feats.readDescendants(I.GeographicPosition.qn).each { featbuGeomProps ->
                            PARSER.GeographicPosition(featbuGeomProps, output_ID, output_props, output_geoms);
                        }
                        break;
                    default:
                        input_Feats.readPrimitive(
                        I.Address.props, output_props,
                        iri(output_ad_ns, input_Feats.qn.getLocalPart())
                        )
                        break;
                }
            }

            output.vertex(output_ID, O.Address.qn,
                    output_props, EMPTY, output_geoms);
        },
        "GeographicPosition": { input_Geom_Feat,output_ID, output_props, output_geoms ->
            input_Geom_Feat.readChildren().each { input_Geom_Feats ->

                switch (input_Geom_Feats.qn) {

                    case I.GeographicPosition.geometry:
                        input_Geom_Feats.readFirstChildGeometry(I.GeographicPosition.geoms, output_geoms,
                        O.Geom);
                        break;
                    default:
                        break;
                }
            }
        }

    ];

    def I = [
        "Address": [
            "qn": qn(input_ns, "Address"),
            "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_ad_ns,
            "beginLifespanVersion",
            "endLifespanVersion"
            ),
            "position": qn(input_ad_ns,"position"),
            "inspireId": qn(input_ad_ns, "inspireId"),
            "Identifier": qn(input_base_ns, "Identifier"),
            "IdentifierProps": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_base_ns,
            "localId",
            "namespace",
            "versionId"
            )

        ],
        "GeographicPosition": [
            "qn": qn(input_ad_ns, "GeographicPosition"),
            "geometry": qn(input_ad_ns, "geometry"),
            "geoms": mapGeometryTypes("http://www.opengis.net/gml/3.2",
            "Point","MultiPoint"
            )
        ]
    ];

    /* Output */

    def O = [
        "Geom": iri("http://oskari.org/spatial#", "location"),
        "Address": [
            "qn": iri(output_ns, "Address"),
            "geometry": iri(output_ns, "geometry")
        ]

    ];


    public void parse() {


        output.prefix("_ns", output_ns);
        output.prefix("_bu", output_ad_ns);

        output.type(O.Address.qn,
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
                iri(output_ad_ns,"beginLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_ad_ns,"endLifespanVersion"),
                XSDDatatype.XSDstring
                )

                ),
                EMPTY,
                geometryTypes(
                pair(O.Geom, "GEOMETRY" )
                )
                );


        /* Process */
        iter(input.root().descendantElementCursor(I.Address.qn)).each { input_Feat ->
            PARSER.Address(input_Feat);
        }
    }
}

