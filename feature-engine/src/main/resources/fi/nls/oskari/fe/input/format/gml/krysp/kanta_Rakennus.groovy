import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class kanta_RakennusParser extends AbstractGroovyGMLParserRecipe.GML3 {

    def input_ns = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
    def output_ns = "http://www.paikkatietopalvelu.fi/gml/kantakartta#";

    def PARSER = [
            "Sijainti": { input_Feat, output_Rakennus_ID ->

                input_Feat.readDescendants(I.Sijainti.qn).each { input_Feats ->

                    def output_ID = O.Sijainti.qn.unique();
                    def output_geoms = geometries();
                    input_Feats.readFirstChildGeometry(
                            I.Sijainti.geoms, output_geoms, O.Geom);
                    output.vertex(output_ID, O.Sijainti.qn,
                            EMPTY, EMPTY, output_geoms);

                    output.edge(output_Rakennus_ID, O.Rakennus.sijainti, output_ID);

                }
            },
            "Rakennus": { input_Feat ->

                def output_ID = O.Rakennus.qn.unique();
                def output_props = properties();

                input_Feat.readChildren().each { input_Feats ->

                    switch (input_Feats.qn) {

                        case I.Rakennus.sijainnit:
                            PARSER.Sijainti(input_Feats, output_ID);
                            break;
                        case I.Rakennus.labelit:
                            PARSER.Label(input_Feats, output_ID);
                            break;
                        default:
                            input_Feats.readPrimitive(I.Rakennus.props, output_props,
                                    iri(output_ns, input_Feats.qn.getLocalPart()))
                            break;


                    }

                }

                output.vertex(output_ID, O.Rakennus.qn,
                        output_props, EMPTY);

            },
            "Label": { input_Feat, output_Rakennus_ID ->

                input_Feat.readDescendants(I.Label.qn).each { feat ->

                    def output_ID = O.Label.qn.unique();
                    def output_props = properties();
                    def output_geoms = geometries();

                    feat.readChildren().each { input_Feats ->

                        switch (input_Feats.qn) {

                            case I.Label.siirtymasijainti:
                                input_Feats.readFirstChildGeometry(I.Label.geoms, output_geoms,
                                        O.Geom);
                                break;

                            default:
                                input_Feats.readPrimitive(I.Label.props, output_props,
                                        iri(output_ns, input_Feats.qn.getLocalPart()))
                                break;

                        }

                    }

                    output.vertex(output_ID, O.Label.qn, output_props, EMPTY, output_geoms);
                    output.edge(output_Rakennus_ID, O.Rakennus.label, output_ID);

                }
            }

    ];

    def I = [
            "Sijainti": [
                    "qn": qn(input_ns, "Sijainti"),
                    "geoms": mapGeometryTypes(input_ns,
                            "keskilinja", "referenssipiste", "reunaviiva", "alue")],
            "Rakennus": [
                    "qn": qn(input_ns, "Rakennus"),
                    "sijainnit": qn(input_ns, "sijainnit"),
                    "labelit": qn(input_ns, "labelit"),
                    "osoite": qn(input_ns, "osoite"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_ns,
                            "sijaintiepavarmuus", "luontitapa", "tila",
                            "rakennustunnus",
                            "rakennuksenKayttotarkoitus",
                            "julkisivumateriaali",
                            "kerrosluku",
                            "korkeusasema"
                    )

            ],
            "Label": [
                    "qn": qn(input_ns, "Label"),
                    "props": mapPrimitiveTypes(XSDDatatype.XSDstring,
                            input_ns,
                            "ilmentymaElementinNimi",
                            "kayttotarkoitus",
                            "suunta",
                            "kohta"
                    ),
                    "siirtymasijainti": qn(input_ns, "siirtymasijainti"),
                    "geoms": mapGeometryTypes("http://www.opengis.net/gml",
                            "Point"
                    )

            ]
    ];

    /* Output */

    def O = [
            "Geom": iri("http://oskari.org/spatial#", "location"),
            "Rakennus": [
                    "qn": iri(output_ns, "Rakennus"),
                    "sijainti": iri(output_ns, "sijainti"),
                    "label": iri(output_ns, "label")
            ],
            "Sijainti": [
                    "qn": iri(output_ns, "Sijainti"),
                    "rakennus": iri(output_ns, "rakennus")
            ],
            "Label": [
                    "qn": iri(output_ns, "Label")
            ]
    ];


    public void parse() {


        output.prefix("_ns", output_ns);
        output.type(O.Sijainti.qn, EMPTY,EMPTY,EMPTY);

        /* Process */
        iter(input.root().descendantElementCursor(I.Rakennus.qn)).each { input_Feat ->
            PARSER.Rakennus(input_Feat);
        }


    }


}