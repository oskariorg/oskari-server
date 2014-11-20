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

import fi.nls.oskari.eu.inspire.util.GeometryProperty;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;

public class FEGeometryDeserializer extends JsonDeserializer<GeometryProperty> {

    public FEGeometryDeserializer() {
    }

    protected Configuration gml;

    protected FEPullParser parserAny;

    public Map<QName, FEPullParser.PullParserHandler> mapGeometryType(
            final QName qname) {
        Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();
        handlers.put(qname, new FEPullParser.ElementPullParserHandler(qname,
                gml));
        return handlers;
    }

    public Map<QName, FEPullParser.PullParserHandler> mapGeometryTypes(
            final String ns, final String... localNames) {
        Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();

        for (String localPart : localNames) {
            QName qname = new QName(ns, localPart);
            handlers.put(qname, new FEPullParser.ElementPullParserHandler(
                    qname, gml));
        }

        return handlers;

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
        FEFromXmlParser parser = (FEFromXmlParser) ctxt.getParser();

        Geometry geom = null;

        XMLStreamReader reader = parser.getStaxReader();

        try {
            // geom = (Geometry) parseGeometry(I_RoadLink_geoms, reader);
            // parser.resetContext();
            parser.skipChildren();

            /*
             * } catch (XMLStreamException e) { // TODO Auto-generated catch
             * block throw new IOException(e); } catch (SAXException e) { throw
             * new IOException(e);
             */
        } finally {
        }

        return new GeometryProperty(geom);
    }

}