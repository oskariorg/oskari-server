import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class fgi_cascade_Parser extends AbstractGroovyGMLParserRecipe.GML32 {

    def input_ns = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0";
    def input_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0"

    def output_ns = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0#";
    def output_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0#"
    //def O;

    def PARSER = [
            "GeographicalName": { input_Feat, output_NamedPlace_ID ->

                def output_ID = O.GeographicalName.qn.unique();
                def output_props = properties();

                input_Feat.readChildren().each { input_Feats ->
                    input_Feats.readPrimitive(I.GeographicalName.props, output_props,
                            iri(output_ns, input_Feats.qn.getLocalPart()));
                }

                output.vertex(output_ID, O.GeographicalName.qn,
                        output_props, EMPTY);
                output.edge(output_NamedPlace_ID, O.NamedPlace.name, output_ID);

            },
            "NamedPlace": { input_Feat ->

                def output_ID = O.NamedPlace.qn.unique();
                def output_props = properties();
                def output_geoms = geometries();

                input_Feat.readChildren().each { input_Feats ->

                    switch (input_Feats.qn) {

                        case I.NamedPlace.geometry:
                            input_Feats.readFirstChildGeometry(I.NamedPlace.geoms, output_geoms,
                                    O.Geom);
                            break;
                        case I.NamedPlace.name:
                            input_Feats.readDescendants(I.GeographicalName.qn).each { featGNProps ->
                                PARSER.GeographicalName(featGNProps, output_ID);
                            }
                            break;
                        default:
                            input_Feats.readPrimitive(
                                    I.NamedPlace.props, output_props,
                                    iri(output_ns, input_Feats.qn.getLocalPart())
                            )
                            break;
                    }
                }

                output.vertex(output_ID, O.NamedPlace.qn,
                        output_props, EMPTY, output_geoms);

            }
    ];

    def I = [
            "NamedPlace": [
                    "qn": qn(input_ns, "NamedPlace"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_gn_ns,
                            "beginLifespanVersion",
                            "endLifespanVersion",
                            "localType"
                    ),
                    "geometry": qn(input_gn_ns, "geometry"),
                    "name": qn(input_gn_ns, "name"),
                    "geoms": mapGeometryTypes("http://www.opengis.net/gml/3.2",
                            "Point"
                    )

            ],
            "GeographicalName": [
                    "qn": qn(input_ns, "GeographicalName"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_gn_ns,
                            "language",
                            "sourceOfName",
                            "pronunciation",
                            "referenceName"
                    )
            ]
    ];

    /* Output */

    def O = [
            "Geom": iri("http://oskari.org/spatial#", "location"),
            "NamedPlace": [
                    "qn": iri(output_ns, "NamedPlace"),
                    "geometry": iri(output_ns, "geometry"),
                    "name": iri(output_gn_ns, "name")
            ],
            "GeographicalName": [
                    "qn": iri(output_ns, "GeographicalName")
            ]
    ];


    public void parse() {


        output.prefix("_ns", output_ns);
        output.type(O.NamedPlace.qn, EMPTY,EMPTY,EMPTY);

        /* Process */
        iter(input.root().descendantElementCursor(I.NamedPlace.qn)).each { input_Feat ->
            PARSER.NamedPlace(input_Feat);
        }


    }


}