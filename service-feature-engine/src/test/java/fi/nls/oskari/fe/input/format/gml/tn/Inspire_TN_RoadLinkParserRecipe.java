package fi.nls.oskari.fe.input.format.gml.tn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.eu.inspire.gmlas.geographicalnames.GeographicalName;
import fi.nls.oskari.eu.inspire.gmlas.roadtransportnetwork.RoadLink;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.StaxMateGMLParserRecipeBase;
import fi.nls.oskari.fe.input.jackson.GeometryProperty;
import fi.nls.oskari.fe.input.jackson.GeometryPropertySerializer;
import fi.nls.oskari.fe.input.jackson.GmlMapper;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;

/* PoC Streaming Parser to Match Groovy Parser in Java 7 */
public class Inspire_TN_RoadLinkParserRecipe extends
        StaxMateGMLParserRecipeBase {

    /* input namespace declarations */
    final String input_ns = "urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0";
    final String input_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
    final String input_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2";
    final String input_base_ns = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/";
    final String input_gml_ns = "http://www.opengis.net/gml/3.2";

    /* output namespace declarations - of interest mostly for JSON-LD */
    final String output_ns = "urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0#";
    final String output_net_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";
    final String output_tn_ns = "urn:x-inspire:specification:gmlas:Network:3.2#";
    final String output_gn_ns = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0#";

    /* input element qualified name declarations */
    /* def I = [ in Groovy ] */
    final QName I_RoadLink_qn = qn(input_ns, "RoadLink");

    /* output element qualified name declarations */
    /* def O = [ in Groovy ]; */
    final Resource O_Geom = iri("http://oskari.org/spatial#", "location");
    final Resource O_RoadLink_qn = iri(output_ns, "RoadLink");
    final Resource O_GeographicalName_qn = iri(output_ns, "GeographicalName");
    final Resource O_SpellingOfName_qn = iri(output_ns, "SpellingOfName");
    final Resource O_SpellingOfName_text = iri(output_gn_ns, "text");

    final List<Pair<Resource, Object>> EMPTY = new ArrayList<Pair<Resource, Object>>();
    final GmlMapper mapper;

    /**
     * 
     */
    public Inspire_TN_RoadLinkParserRecipe() {

        /* def I = [ in groovy ]; */
        /* setup GML version information for geotools based pull parser */
        gml = new org.geotools.gml3.v3_2.GMLConfiguration(true);
        parserAny = new FEPullParser(gml, null);

        /* setup input element qualified names and mappings */

        mapper = new GmlMapper(gml);
        mapper.getGeometryDeserializer().mapGeometryTypes(
                "http://www.opengis.net/gml/3.2", "LineString", "Curve",
                "CompositeCurve", "OrientableCurve", "MultiCurve");

    }

    // TODO properly support multiple languages
    /*
     * output.vertex(O_RoadLink_qn.unique(), O_RoadLink_qn, output_props, EMPTY,
     * output_geoms);
     */

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
            simpleProperties.add(pair(iri(output_ns, "inspireId"),
                    XSDDatatype.XSDany));
            simpleProperties.add(pair(iri(output_ns, "geographicalNames"),
                    XSDDatatype.XSDany));

            final List<Pair<Resource, Object>> linkProperties = new ArrayList<Pair<Resource, Object>>();
            final List<Pair<Resource, String>> geometryProperties = new ArrayList<Pair<Resource, String>>();
            geometryProperties.add(pair(O_Geom, "GEOMETRY"));

            output.type(iri(output_ns, "RoadLink"), simpleProperties,
                    linkProperties, geometryProperties);

            Iterator<InputEvent> iter = iter(((StaxGMLInputProcessor) input)
                    .root().descendantElementCursor(I_RoadLink_qn));

            ObjectMapper json = new ObjectMapper();
            SimpleModule gmlModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
            gmlModule.addSerializer(GeometryProperty.class,new GeometryPropertySerializer()); 
            json.registerModule(gmlModule);


            while (iter.hasNext()) {

                System.out.println("NNN");
                InputEvent input_Feat = iter.next();

                RoadLink roadLink = mapper.readValue(
                        input_Feat.crsr.getStreamReader(), RoadLink.class);

                System.out.println(json.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(roadLink));

                String gmlid = roadLink.id;
                Resource output_ID = O_RoadLink_qn.unique(gmlid);
                List<Pair<Resource, Object>> output_props = new ArrayList<Pair<Resource, Object>>();
                List<Pair<Resource, Geometry>> output_geoms = new ArrayList<Pair<Resource, Geometry>>();

                if( roadLink.centrelineGeometry!=null && roadLink.centrelineGeometry.geometry != null) {
                    output_geoms.add(pair(O_Geom, roadLink.centrelineGeometry.geometry));
                }
                
                if (roadLink.geographicalName != null) {
                    for (GeographicalName gn : roadLink.geographicalName) {
                        output.vertex(O_RoadLink_qn.unique(), O_RoadLink_qn,
                                output_props, EMPTY, output_geoms);
                    }
                }

            }

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

    }

}