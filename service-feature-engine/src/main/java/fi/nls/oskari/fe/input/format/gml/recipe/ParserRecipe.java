package fi.nls.oskari.fe.input.format.gml.recipe;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import fi.nls.oskari.fi.rysp.generic.WFS11_path_parse_worker;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ParserRecipe {
    public List<Pair<Resource, Geometry>> geometries(
            Pair<Resource, Geometry>... pairs);

    public List<Pair<Resource, String>> geometryTypes(
            Pair<Resource, String>... pairs);

    public Resource iri();

    public Resource iri(final String base);

    public Resource iri(final String base, final String localPart);

    public Map<QName, FEPullParser.PullParserHandler> mapGeometryType(
            final QName qname);

    public Map<QName, FEPullParser.PullParserHandler> mapGeometryTypes(
            final String ns, final String... localNames);

    public Pair<Resource, Geometry> pair(Resource rc, Geometry val);

    public Pair<Resource, ?> pair(Resource rc, Object val);

    public void parse() throws IOException;

    public List<Pair<Resource, Object>> properties(
            Pair<Resource, Object>... pairs);

    public QName qn(final String ns, final String localName);

    public void setInputOutput(InputProcessor input, OutputProcessor output);

    public List<Pair<Resource, Resource>> simpleTypes(
            Pair<Resource, Resource>... pairs);

    public Resource xsd(XSDDatatype xsd);

    public void setParseWorker(ELF_path_parse_worker worker);
    public void setWFS11ParseWorker(WFS11_path_parse_worker worker);
}