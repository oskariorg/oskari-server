package fi.nls.oskari.fe.input.format.gml.tn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.tuple.Pair;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.StaxMateGMLParserRecipeBase;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;

/* PoC Streaming Parser to Match Groovy Parser in Java 7 */
public class ELF_TN_RoadLinkParserRecipe extends StaxMateGMLParserRecipeBase {

    /* input namespace declarations */
    final String input_ns = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0";
    final String input_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
    final String input_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2";
    final String input_base_ns = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/";
    final String input_gml_ns = "http://www.opengis.net/gml/3.2";

    /* output namespace declarations - of interest mostly for JSON-LD */
    final String output_ns = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0#";
    final String output_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";
    final String output_tn_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";

    final String output_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0#";

    /* input element qualified name declarations */
    /* def I = [ in Groovy ] */
    final QName I_RoadLink_qn;
    final QName I_RoadLink_geometry;
    final QName I_RoadLink_inspireId;
    final QName I_RoadLink_Identifier;
    final QName I_RoadLink_geographicalName;

    final QName I_GeographicalName_qn;
    final QName I_GeographicalName_spelling;
    final Map<QName, Resource> I_GeographicalName_props;

    final QName I_SpellingOfName_qn;
    final Map<QName, Resource> I_SpellingOfName_props;

    final Map<QName, PullParserHandler> I_RoadLink_geoms;
    final Map<QName, Resource> I_RoadLink_props;

    /* output element qualified name declarations */
    /* def O = [ in Groovy ]; */
    final Resource O_Geom = iri("http://oskari.org/spatial#", "location");
    final Resource O_RoadLink_qn = iri(output_ns, "RoadLink");
    final Resource O_GeographicalName_qn = iri(output_ns, "GeographicalName");
    final Resource O_SpellingOfName_qn = iri(output_ns, "SpellingOfName");
    final Resource O_SpellingOfName_text = iri(output_gn_ns, "text");

    final List<Pair<Resource, Object>> EMPTY = new ArrayList<Pair<Resource, Object>>();

    /**
     * 
     */
    public ELF_TN_RoadLinkParserRecipe() {

        /* def I = [ in groovy ]; */
        /* setup GML version information for geotools based pull parser */
        gml = new org.geotools.gml3.v3_2.GMLConfiguration(true);
        parserAny = new FEPullParser(gml, null);

        /* setup input element qualified names and mappings */
        I_RoadLink_qn = qn(input_ns, "RoadLink");
        I_RoadLink_props = mapPrimitiveTypes(XSDDatatype.XSDstring,
                input_net_ns, "beginLifespanVersion", "endLifespanVersion",
                "localType");

        I_RoadLink_geometry = qn(input_net_ns, "centrelineGeometry");
        I_RoadLink_geoms = mapGeometryTypes("http://www.opengis.net/gml/3.2",
                "LineString", "Curve", "CompositeCurve", "OrientableCurve",
                "MultiCurve");

        I_RoadLink_inspireId = qn(input_ns, "inspireId");
        I_RoadLink_geographicalName = qn(input_net_ns, "geographicalName");
        I_RoadLink_Identifier = qn(input_base_ns, "Identifier");

        I_GeographicalName_qn = qn(input_gn_ns, "GeographicalName");
        I_GeographicalName_spelling = qn(input_gn_ns, "spelling");
        I_GeographicalName_props = mapPrimitiveTypes(XSDDatatype.XSDstring,
                input_gn_ns, "language", "sourceOfName", "pronunciation",
                "referenceName");

        I_SpellingOfName_qn = qn(input_gn_ns, "SpellingOfName");
        I_SpellingOfName_props = mapPrimitiveTypes(XSDDatatype.XSDstring,
                input_gn_ns, "text", "script");

    }

    private void PARSER_GeographicalName(InputEvent input_Feat,
            Resource output_NamedPlace_ID,
            List<Pair<Resource, Object>> output_props,
            List<Pair<Resource, Geometry>> output_geoms)
            throws XMLStreamException, IOException {

        Resource output_ID = O_GeographicalName_qn.unique();
        // def output_props = properties();

        Iterator<InputEvent> input_Feats_iter = input_Feat.readChildren();
        while (input_Feats_iter.hasNext()) {
            InputEvent input_Feats = input_Feats_iter.next();

            if (input_Feats.qn.equals(I_GeographicalName_spelling)) {
                Iterator<InputEvent> featGNProps_iter = input_Feats
                        .readDescendants(I_SpellingOfName_qn);
                while (featGNProps_iter.hasNext()) {
                    InputEvent featGNProps = featGNProps_iter.next();

                    PARSER_SpellingOfName(featGNProps, output_ID, output_props,
                            output_geoms);
                }
            } else {
                input_Feats.readPrimitive(I_GeographicalName_props,
                        output_props,
                        iri(output_gn_ns, input_Feats.qn.getLocalPart()));
            }

        }

        // TODO properly support multiple languages
        output.vertex(/* output_ID */O_RoadLink_qn.unique(), O_RoadLink_qn,
                output_props, EMPTY, output_geoms);

    }

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

                input_Feats.readFirstChildGeometry(I_RoadLink_geoms,
                        output_geoms, O_Geom);

            } else if (input_Feats.qn.equals(I_RoadLink_inspireId)) {

            } else if (input_Feats.qn.equals(I_RoadLink_geographicalName)) {
                Iterator<InputEvent> iterGeographicalNames = input_Feats
                        .readDescendants(I_GeographicalName_qn);
                while (iterGeographicalNames.hasNext()) {
                    InputEvent featGNProps = iterGeographicalNames.next();
                    PARSER_GeographicalName(featGNProps, output_ID,
                            output_props, output_geoms);
                    placeNamesCount++;
                }

            } else {
                input_Feats.readPrimitive(I_RoadLink_props, output_props,
                        iri(output_tn_ns, input_Feats.qn.getLocalPart()));
            }
        }

        if (placeNamesCount == 0) {

            output.vertex(output_ID, O_RoadLink_qn, output_props, EMPTY,
                    output_geoms);

        }

    }

    private void PARSER_SpellingOfName(InputEvent input_Feat,
            Resource output_GeographicalName_ID,
            List<Pair<Resource, Object>> output_props,
            List<Pair<Resource, Geometry>> output_geoms)
            throws XMLStreamException, IOException {

        Resource output_ID = O_SpellingOfName_qn.unique();

        Iterator<InputEvent> input_Feats_iter = input_Feat.readChildren();
        while (input_Feats_iter.hasNext()) {
            InputEvent input_Feats = input_Feats_iter.next();

            input_Feats.readPrimitive(I_SpellingOfName_props, output_props,
                    iri(output_gn_ns, input_Feats.qn.getLocalPart()));

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
            simpleProperties.add(pair(iri(output_tn_ns, "endLifespanVersion"),
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
                    .root().descendantElementCursor(I_RoadLink_qn));
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