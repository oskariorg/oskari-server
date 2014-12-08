package fi.nls.oskari.fe.input.jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.geotools.xml.Configuration;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;

public class GeometryPropertyDeserializer extends
        JsonDeserializer<GeometryProperty> {

    protected Configuration gml;
    protected FEPullParser parserAny;
    protected final Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();

    public GeometryPropertyDeserializer(Configuration gml,
            FEPullParser parserAny) {
        this.gml = gml;
        this.parserAny = parserAny;
    }

    public Map<QName, FEPullParser.PullParserHandler> mapGeometryType(
            final QName qname) {
        Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();
        handlers.put(qname, new FEPullParser.ElementPullParserHandler(qname,
                gml));
        return handlers;
    }

    public void mapGeometryTypes(final String ns, final String... localNames) {

        for (String localPart : localNames) {
            QName qname = new QName(ns, localPart);
            handlers.put(qname, new FEPullParser.ElementPullParserHandler(
                    qname, gml));
        }

        ;

    }

    public Object parseGeometry(
            Map<QName, FEPullParser.PullParserHandler> handlers,
            XMLStreamReader reader) throws XMLStreamException, IOException,
            SAXException {
        QName qn = reader.getName();
        PullParserHandler handler = handlers.get(qn);
        Object obj = null;
        if (handler != null) {
            parserAny.setHandler(handler);
            parserAny.setPp(reader);

            obj = parserAny.parse();

        }

        return obj;
    }

    @Override
    public GeometryProperty deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        // TODO Auto-generated method stub
        FromXmlParser parser = (FromXmlParser) ctxt.getParser();

        Geometry geom = null;

        XMLStreamReader reader = parser.getStaxReader();

        try {
            geom = (Geometry) parseGeometry(handlers, reader);
            parser.resume();
            // parser.skipChildren();

        } catch (XMLStreamException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);

        } finally {
        }

        return new GeometryProperty(geom);
    }
}