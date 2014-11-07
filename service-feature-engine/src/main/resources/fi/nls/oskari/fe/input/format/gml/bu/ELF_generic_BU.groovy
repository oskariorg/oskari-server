import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class ELF_generic_bu_Parser extends AbstractGroovyGMLParserRecipe.GML32 {

    def input_ns = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
    def input_bu_ns = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3"
    def input_base_ns = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/"
    def input_geom2d_ns = "http://inspire.ec.europa.eu/draft-schemas/bu-core2d/3.0rc3";
    def input_gml_ns = "http://www.opengis.net/gml/3.2";

    def output_ns = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0#";
    def output_bu_ns = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3#";

    def PARSER = [
        "Building": { input_Feat ->

            def gmlid = input_Feat.attr(input_gml_ns, "id");
            def output_ID = O.Building.qn.unique(gmlid);
            def output_props = properties();
            def output_geoms = geometries();

            input_Feat.readChildren().each { input_Feats ->

                switch (input_Feats.qn) {
                    case I.Building.inspireId:
                        input_Feats.readDescendants(I.Building.Identifier).each { featIdentifierProps ->

                            featIdentifierProps.readChildren().each { featIdProps ->
                                featIdProps.readPrimitive(
                                        I.Building.IdentifierProps, output_props,
                                        iri(output_ns, featIdProps.qn.getLocalPart())
                                        )
                            }
                        }
                        break;
                    case I.Building.geometry2D:
                        input_Feats.readDescendants(I.BuildingGeometry2D.qn).each { featbuGeomProps ->
                            PARSER.BuildingGeometry2D(featbuGeomProps, output_ID, output_props, output_geoms);
                        }
                        break;
                    default:
                        input_Feats.readPrimitive(
                        I.Building.props, output_props,
                        iri(output_bu_ns, input_Feats.qn.getLocalPart())
                        )
                        break;
                }
            }

            output.vertex(output_ID, O.Building.qn,
                    output_props, EMPTY, output_geoms);
        },
        "BuildingGeometry2D": { input_Geom_Feat,output_ID, output_props, output_geoms ->
            input_Geom_Feat.readChildren().each { input_Geom_Feats ->

                switch (input_Geom_Feats.qn) {

                    case I.BuildingGeometry2D.geometry:
                        input_Feats.readFirstChildGeometry(I.BuildingGeometry2D.geoms, output_geoms,
                        O.Geom);
                        break;
                    default:
                        break;
                }
            }
        }

    ];

    def I = [
        "Building": [
            "qn": qn(input_ns, "Building"),
            "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_bu_ns,
            "beginLifespanVersion",
            "endLifespanVersion"
            ),
            "geometry2D": qn(input_geom2d_ns,"geometry2D"),
            "inspireId": qn(input_bu_ns, "inspireId"),
            "Identifier": qn(input_base_ns, "Identifier"),
            "IdentifierProps": mapPrimitiveTypes(XSDDatatype.XSDstring,
            input_base_ns,
            "localId",
            "namespace",
            "versionId"
            )

        ],
        "BuildingGeometry2D": [
            "qn": qn(input_bu_ns, "BuildingGeometry2D"),
            "geometry": qn(input_bu_ns, "geometry"),
            "geoms": mapGeometryTypes("http://www.opengis.net/gml/3.2",
            "Polygon","Surface", "PolyhedralSurface","TriangulatedSurface","Tin","OrientableSurface", "CompositeSurface"
            )
        ]
    ];

    /* Output */

    def O = [
        "Geom": iri("http://oskari.org/spatial#", "location"),
        "Building": [
            "qn": iri(output_ns, "Building"),
            "geometry": iri(output_ns, "geometry")
        ]

    ];


    public void parse() {


        output.prefix("_ns", output_ns);
        output.prefix("_bu", output_bu_ns);

        output.type(O.Building.qn,
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
                iri(output_bu_ns,"beginLifespanVersion"),
                XSDDatatype.XSDstring
                ),
                pair(
                iri(output_bu_ns,"endLifespanVersion"),
                XSDDatatype.XSDstring
                )

                ),
                EMPTY,
                geometryTypes(
                pair(O.Geom, "GEOMETRY" )
                )
                );


        /* Process */
        iter(input.root().descendantElementCursor(I.Building.qn)).each { input_Feat ->
            PARSER.Building(input_Feat);
        }
    }
}
