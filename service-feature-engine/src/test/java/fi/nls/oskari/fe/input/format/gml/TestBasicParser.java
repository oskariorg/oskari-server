package fi.nls.oskari.fe.input.format.gml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.styling.Style;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import fi.nls.oskari.fe.input.format.gml.recipe.PullParserGMLParserRecipe;
import fi.nls.oskari.fe.input.format.gml.recipe.StaxMateGMLParserRecipeBase;
import fi.nls.oskari.fe.input.format.gml.recipe.StaxMateXMLParserRecipeBase.InputEvent;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class TestBasicParser {

    /* PoC to Match Groovy Parser in Java 7 */
    class TN_Parser_PullParserGMLParserRecipe extends
            StaxMateGMLParserRecipeBase {

        String input_ns = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0";
        String input_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        String input_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2";
        String input_base_ns = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/";
        String input_gml_ns = "http://www.opengis.net/gml/3.2";

        String output_ns = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0#";
        String output_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";
        String output_tn_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";
        String output_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0#";

        ImmutablePair<Resource, XSDDatatype> pair(Resource rc, XSDDatatype val) {
            return new ImmutablePair<Resource, XSDDatatype>(rc, val);
        }

        ImmutablePair<Resource, String> pair(Resource rc, String val) {
            return new ImmutablePair<Resource, String>(rc, val);
        }

        final QName I_RoadLink_geometry = qn(input_net_ns, "centrelineGeometry");
        final QName I_RoadLink_inspireId = qn(input_ns, "inspireId");
        final QName I_RoadLink_geographicalName = qn(input_net_ns,
                "geographicalName");
        final Map<QName, PullParserHandler> I_RoadLink_geoms = mapGeometryTypes(
                "http://www.opengis.net/gml/3.2", "LineString", "Curve",
                "CompositeCurve", "OrientableCurve", "MultiCurve");

        Resource O_Geom = iri("http://oskari.org/spatial#", "location");
        Resource O_RoadLink_qn = iri(output_ns, "RoadLink");

        private void PARSER_RoadLink(InputEvent input_Feat)
                throws XMLStreamException, IOException, SAXException {

            String gmlid = input_Feat.attr(input_gml_ns, "id");
            Resource output_ID = O_RoadLink_qn.unique(gmlid);
            List<Pair<Resource, Object>> output_props = new ArrayList<Pair<Resource, Object>>();
            List<Pair<Resource, Geometry>> output_geoms = new ArrayList<Pair<Resource, Geometry>>();

            int placeNamesCount = 0;

            Iterator<InputEvent> iter = input_Feat.readChildren();
            while (iter.hasNext()) {
                InputEvent input_Feats = iter.next();

                if (input_Feats.qn.equals(I_RoadLink_geometry)) {
                /*    input_Feats.readFirstChildGeometry(I_RoadLink_geoms,
                            output_geoms, O_Geom);
*/
                } else if (input_Feats.qn.equals(I_RoadLink_inspireId)) {

                } else if (input_Feats.qn.equals(I_RoadLink_geographicalName)) {

                } else {

                }
            }

            if (placeNamesCount == 0) {

                List<Pair<Resource, Object>> EMPTY = new ArrayList<Pair<Resource, Object>>();

                output.vertex(output_ID, O_RoadLink_qn, output_props, EMPTY,
                        output_geoms);

            }

        }

        @Override
        public void parse() throws IOException {
            try {
                output.prefix("_ns", output_ns);
                output.prefix("_tn", output_tn_ns);

                /* Declare a Type for Transport */

                final List<Pair<Resource, XSDDatatype>> simpleProperties = new ArrayList<Pair<Resource, XSDDatatype>>();

                simpleProperties.add(pair(iri(output_ns, "localId"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_ns, "namespace"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_ns, "versionId"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(
                        iri(output_tn_ns, "beginLifespanVersion"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(
                        iri(output_tn_ns, "endLifespanVersion"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_tn_ns, "localType"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_gn_ns, "language"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_gn_ns, "sourceOfName"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_gn_ns, "pronunciation"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_gn_ns, "referenceName"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_gn_ns, "text"),
                        XSDDatatype.XSDstring));
                simpleProperties.add(pair(iri(output_gn_ns, "script"),
                        XSDDatatype.XSDstring));

                final List<Pair<Resource, Object>> linkProperties = new ArrayList<Pair<Resource, Object>>();
                final List<Pair<Resource, String>> geometryProperties = new ArrayList<Pair<Resource, String>>();
                geometryProperties.add(pair(O_Geom, "GEOMETRY"));

                output.type(iri(output_ns, "RoadLink"), simpleProperties,
                        linkProperties, geometryProperties);

                int fcount = 0;

                Iterator<InputEvent> iter = iter(((StaxGMLInputProcessor) input)
                        .root().descendantElementCursor(
                                qn(input_ns, "RoadLink")));
                while (iter.hasNext()) {
                    InputEvent input_Feat = iter.next();

                    PARSER_RoadLink(input_Feat);
                    fcount++;
                }

            } catch (XMLStreamException e) {
                throw new IOException(e);
            } catch (SAXException e) {

                throw new IOException(e);
            }

        }

    }

    /**
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws XMLStreamException
     */
    @Test
    public void test_NlsFi_TN_WFS_GMLtoPNG() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        Style sldStyle = MapContentOutputProcessor
                .createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld");

        OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
                "EPSG:3857", sldStyle);

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/fe/input/format/gml/tn/nls_fi-ELF-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            FileOutputStream fouts = new FileOutputStream("TN-nls_fi.png");
            try {
                outputProcessor.setOutput(fouts);

                PullParserGMLParserRecipe recipe = new TN_Parser_PullParserGMLParserRecipe();
                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                fouts.close();
            }

        } finally {
            inp.close();
        }

    }
}
